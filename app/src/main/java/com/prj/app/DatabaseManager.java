package com.prj.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.Nullable;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String SCAN_RESULT_TAB = "SCAN_RESULT_TAB";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    private static final String BSSID_SCAN_TAB = "BSSID_SCAN_TAB";
    private static final String SETTINGS_TAB = "SETTINGS_TAB";

    public DatabaseManager(@Nullable Context context) {
        super(context, "prj-app-db.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + SCAN_RESULT_TAB + " (\n" +
                "\t" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t" + COLUMN_TIMESTAMP + " DATE(1) NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                ")";

        db.execSQL(createTableStatement);

        createTableStatement = "CREATE TABLE " + BSSID_SCAN_TAB + " (\n" +
                "\t ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t SCAN_RESULT_ID INTEGER NOT NULL,\n" +
                "\t BSSID TEXT NOT NULL,\n" +
                "\t DISTANCE NUMERIC NOT NULL\n" +
                ");";

        db.execSQL(createTableStatement);

        createTableStatement = "CREATE TABLE " + SETTINGS_TAB + " (\n" +
                "\t NAME TEXT PRIMARY KEY,\n" +
                "\t VALUE TEXT NOT NULL \n" +
                ");";


        db.execSQL(createTableStatement);
        String insertSettingsQuery = "INSERT INTO " + SETTINGS_TAB + " (NAME, VALUE) VALUES ('SAVE_HOTSPOT_LOCATION', 0)";
        db.execSQL(insertSettingsQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addScan(List<ScanResult> scanList) {
        if (scanList.size() < 1) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase(); //locking database when using writable
        db.execSQL("INSERT INTO SCAN_RESULT_TAB DEFAULT VALUES;");  //INSERT NEW SCAN RESULT ID AND TIMESTAMP
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
        SQLiteDatabase db = this.getReadableDatabase();
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

    public void setSaveHotspotLocation(boolean value){
        int valueToInsert = value ? 1 : 0;
        String query = "UPDATE "+ SETTINGS_TAB + " SET VALUE = " + value + " WHERE NAME = 'SAVE_HOTSPOT_LOCATION'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    public boolean canSaveHotspotLocation(){
        ArrayList<Integer> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
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
        if(results.size() == 0){
            return false;
        }
        return results.get(0) == 1;
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.floor(Math.pow(10.0, exp) * 100) / 100;
    }


    private String getSQLiteDate(Date date) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(date.getTime());
        return sdf.format(timestamp);
    }
}
