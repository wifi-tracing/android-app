package com.prj.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint({"StaticFieldLeak", "SimpleDateFormat"})
public class DatabaseManager extends SQLiteOpenHelper {
    private static final String SCAN_RESULT_TAB = "SCAN_RESULT_TAB";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    private static final String BSSID_SCAN_TAB = "BSSID_SCAN_TAB";
    private static final String SETTINGS_TAB = "SETTINGS_TAB";
    private static final String LOCATION_TAB = "LOCATION_TAB";
    private static final int COORDINATES_PRECISION = 5;
    private static final String DB_PATH = "prj-app-db.db";
    private static DatabaseManager instance;

    private final Context context;

    private DatabaseManager(@Nullable Context context) {
        super(context, DB_PATH, null, 1);
        this.context = context;
        InitializeSQLCipher();
    }

    public static DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = String.format("CREATE TABLE %s (\n\t%s INTEGER PRIMARY KEY AUTOINCREMENT,\n\t%s TEXT NOT NULL\n)", SCAN_RESULT_TAB, COLUMN_ID, COLUMN_TIMESTAMP);
        db.execSQL(createTableStatement);
        createTableStatement = String.format("CREATE TABLE %s (\n\t ID INTEGER PRIMARY KEY AUTOINCREMENT,\n\t SCAN_RESULT_ID INTEGER NOT NULL,\n\t BSSID TEXT NOT NULL,\n\t DISTANCE NUMERIC NOT NULL\n);", BSSID_SCAN_TAB);
        db.execSQL(createTableStatement);
        createTableStatement = String.format("CREATE TABLE %s (\n\t NAME TEXT PRIMARY KEY,\n\t VALUE TEXT NOT NULL \n);", SETTINGS_TAB);
        db.execSQL(createTableStatement);
        createTableStatement = String.format("CREATE TABLE %s (\n\t bssid TEXT PRIMARY KEY,\n\t lat NUMBER NOT NULL, \n\t lng NUMBER NOT NULL \n);", LOCATION_TAB);
        db.execSQL(createTableStatement);

        String insertSettingsQuery = String.format("INSERT INTO %s (NAME, VALUE) VALUES ('SAVE_HOTSPOT_LOCATION', 0)", SETTINGS_TAB);
        db.execSQL(insertSettingsQuery);
    }

    /**
     * Initialise an encrypted SQLite database if it doesn't exist
     */
    private void InitializeSQLCipher() {
        try {
            CryptoManager.generateDatabasePassword(context);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        SQLiteDatabase.loadLibs(context);
        String databaseFile = context.getDatabasePath(DB_PATH).getPath();
        SQLiteDatabase.openOrCreateDatabase(databaseFile, CryptoManager.getDatabasePassword(context), null);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addLocationData(List<ScanResult> results, Location location) {
        SQLiteDatabase readableDb = this.getReadableDatabase(CryptoManager.getDatabasePassword(context));
        SQLiteDatabase writeableDb = this.getWritableDatabase(CryptoManager.getDatabasePassword(context));

        for (ScanResult result : results) {

            String bssid = result.BSSID.replaceAll(":", "").toUpperCase();

            String query = String.format("SELECT * FROM %s WHERE bssid = '%s'", LOCATION_TAB, bssid);
            Cursor cursor = readableDb.rawQuery(query, null);
            List<LatLng> list = new ArrayList<>();
            if (cursor.moveToFirst() && cursor.getCount() == 1) {
                list.add(new LatLng(cursor.getDouble(1), cursor.getDouble(2)));
            } else if (cursor.getCount() == 0) {
                String insertQuery = String.format("INSERT INTO %s (bssid, lat, lng) VALUES ('%s',%s,%s)",
                        LOCATION_TAB,
                        bssid,
                        Util.round(location.getLatitude(), COORDINATES_PRECISION),
                        Util.round(location.getLongitude(), COORDINATES_PRECISION));
                writeableDb.execSQL(insertQuery);
                continue;
            }
            cursor.close();

            list.add(new LatLng(location.getLatitude(), location.getLongitude()));
            LatLng average = Util.getLocationAverage(list);

            String updateQuery = String.format("UPDATE %s SET lat = %s, lng = %s WHERE bssid = '%s'",
                    LOCATION_TAB,
                    Util.round(average.latitude, COORDINATES_PRECISION),
                    Util.round(average.longitude, COORDINATES_PRECISION),
                    bssid);
            writeableDb.execSQL(updateQuery);
        }
    }

    public void deleteData() {
        SQLiteDatabase db = this.getWritableDatabase(CryptoManager.getDatabasePassword(context));
        db.execSQL(String.format("DELETE FROM %s;", BSSID_SCAN_TAB));
        db.execSQL(String.format("DELETE FROM %s;", SCAN_RESULT_TAB));
    }

    /**
     * Add wifi scan result to database
     *
     * @param scanList list of ScanResult objects
     */
    public void addScan(List<ScanResult> scanList) {
        if (scanList.size() < 1) {
            return;
        }
        String isoTimestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        SQLiteDatabase db = this.getWritableDatabase(CryptoManager.getDatabasePassword(context)); //locking database when using writable
        db.execSQL("INSERT INTO SCAN_RESULT_TAB (" + COLUMN_TIMESTAMP + ") VALUES ('" + isoTimestamp + "');");  //INSERT NEW SCAN RESULT ID AND TIMESTAMP
        int lastScanResultId = getLastScanResultId(db);
        StringBuilder queryString = new StringBuilder();


        queryString.append("INSERT INTO " + BSSID_SCAN_TAB + " (BSSID, DISTANCE, SCAN_RESULT_ID)\nVALUES\n");
        for (ScanResult scan : scanList) {
            queryString.append("(");
            queryString.append("'").append(scan.BSSID).append("',").append(calculateDistance(scan.level, scan.frequency)).append(",").append(lastScanResultId);
            queryString.append("),\n");
        }
        queryString.setLength(queryString.length() - 2); //remove last ,\n
        db.execSQL(queryString.toString());
        db.close();
    }

    /**
     * Get the last ID used in the SCAN_RESULT_TAB
     *
     * @param db database
     * @return the last ID use in SCAN_RESULT_TAB
     */
    public int getLastScanResultId(SQLiteDatabase db) {
        String query = "SELECT MAX(ID) AS ID FROM " + SCAN_RESULT_TAB;
        Cursor cursor = db.rawQuery(query, null);
        int result = -1;
        if (cursor.moveToFirst() && cursor.getCount() == 1) {
            result = cursor.getInt(0); //first column
        }
        cursor.close();
        return result;
    }

    public List<String> getScanBSSIDs() {
        ArrayList<String> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(CryptoManager.getDatabasePassword(context));
        String query = "SELECT DISTINCT B.BSSID\n" +
                "FROM   " + BSSID_SCAN_TAB + " B\n" +
                "       INNER JOIN " + SCAN_RESULT_TAB + " A\n" +
                "               ON B.SCAN_RESULT_ID = A.ID\n" +
                "WHERE  Cast (( JULIANDAY(CURRENT_TIMESTAMP) - JULIANDAY(A.TIMESTAMP) ) AS\n" +
                "             INTEGER) < 14";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                results.add(cursor.getString(0));
            } catch (Exception e) {
                Log.d("TAG_NAME", e.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();
        return results;
    }


    public List<String[]> getRawScanData() {
        ArrayList<String[]> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(CryptoManager.getDatabasePassword(context));
        String query = "SELECT B.BSSID, B.DISTANCE, A.TIMESTAMP \n" +
                "FROM   " + BSSID_SCAN_TAB + " B\n" +
                "       INNER JOIN " + SCAN_RESULT_TAB + " A\n" +
                "               ON B.SCAN_RESULT_ID = A.ID\n" +
                "WHERE  Cast (( JULIANDAY(CURRENT_TIMESTAMP) - JULIANDAY(A.TIMESTAMP) ) AS\n" +
                "             INTEGER) < 14";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                String[] row = {cursor.getString(0), cursor.getString(1), cursor.getString(2)};
                results.add(row);
            } catch (Exception e) {
                Log.d("wifi", e.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();
        return results;
    }

    public JSONArray getRawLocationData() {
        JSONArray results = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase(CryptoManager.getDatabasePassword(context));
        String query = String.format("SELECT * FROM %s", LOCATION_TAB);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                JSONObject row = new JSONObject();
                row.put("bssid", cursor.getString(0));
                row.put("lat", cursor.getDouble(1));
                row.put("lng", cursor.getDouble((2)));
                results.put(row);
            } catch (Exception e) {
                Log.d("wifi", e.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();
        return results;
    }

    public List<Scan> getScanData() {
        ArrayList<Scan> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(CryptoManager.getDatabasePassword(context));
        String query = "SELECT DISTINCT B.BSSID, B.DISTANCE, A.TIMESTAMP \n" +
                "FROM   " + BSSID_SCAN_TAB + " B\n" +
                "       INNER JOIN " + SCAN_RESULT_TAB + " A\n" +
                "               ON B.SCAN_RESULT_ID = A.ID\n" +
                "WHERE  Cast (( JULIANDAY(CURRENT_TIMESTAMP) - JULIANDAY(A.TIMESTAMP) ) AS\n" +
                "             INTEGER) < 14";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                ZonedDateTime zdt = ZonedDateTime.parse(cursor.getString(2), formatter);
                Scan row = new Scan(cursor.getString(0), cursor.getDouble(1), Date.from(zdt.toInstant()));
                results.add(row);
            } catch (Exception e) {
                Log.d("TAG_NAME", e.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();
        return results;
    }

    public void setSaveHotspotLocation(boolean value) {
        int valueToInsert = value ? 1 : 0;
        String query = "UPDATE " + SETTINGS_TAB + " SET VALUE = " + value + " WHERE NAME = 'SAVE_HOTSPOT_LOCATION'";
        SQLiteDatabase db = this.getWritableDatabase(CryptoManager.getDatabasePassword(context));
        db.execSQL(query);
    }

    public boolean canSaveHotspotLocation() {
        ArrayList<Integer> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(CryptoManager.getDatabasePassword(context));
        String query = "SELECT VALUE FROM SETTINGS_TAB WHERE NAME = 'SAVE_HOTSPOT_LOCATION'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                results.add(cursor.getInt(0));
            } catch (Exception e) {
                Log.d("TAG_NAME", e.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();
        if (results.size() == 0) {
            return false;
        }
        return results.get(0) == 1;
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.floor(Math.pow(10.0, exp) * 100) / 100;
    }
}
