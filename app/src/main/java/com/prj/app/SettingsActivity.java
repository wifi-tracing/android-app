package com.prj.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n", "SimpleDateFormat"})
public class SettingsActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private BSSIDMatcher bssidMatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_dev_utils);
        databaseManager = new DatabaseManager(this);
        bssidMatcher = new BSSIDMatcher(databaseManager,
                findViewById(R.id.resultTextView),
                this.getApplicationContext());
        initialiseSettings();
    }

    public void toggleLocationSwitch(View view) {
        Switch switchView = findViewById(R.id.locationSwitch);
        boolean newValue = !databaseManager.canSaveHotspotLocation();
        databaseManager.setSaveHotspotLocation(newValue);
        switchView.setChecked(newValue);
    }

    private void initialiseSettings() {
        Switch switchView = findViewById(R.id.locationSwitch);
        switchView.setChecked(databaseManager.canSaveHotspotLocation());

        EditText maxDistEditText = findViewById(R.id.maxDistEditText);
        EditText maxTimeEditText = findViewById(R.id.maxTimeEditText);
        EditText minHotspotsEditText = findViewById(R.id.minHotspotsEditExt);
        EditText minTimestampsEditText = findViewById(R.id.minTimestampsEditText);

        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());

        maxDistEditText.setHint("DIST " + bssidMatcher.getMAX_DISTANCE_DIFFERENCE() + "m");
        maxTimeEditText.setHint("TIME " + bssidMatcher.getMAX_TIME_DIFFERENCE() + "s");
        minHotspotsEditText.setHint("HOTSPOTS " + bssidMatcher.getMIN_NUMBER_OF_NEAR_HOTSPOTS());
        minTimestampsEditText.setHint("TIMESTAMPS " + bssidMatcher.getMIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS());

    }

    public void updateSettings(View view) {
        EditText maxDistEditText = findViewById(R.id.maxDistEditText);
        EditText maxTimeEditText = findViewById(R.id.maxTimeEditText);
        EditText minHotspotsEditText = findViewById(R.id.minHotspotsEditExt);
        EditText minTimestampsEditText = findViewById(R.id.minTimestampsEditText);

        if (maxDistEditText.getText().toString().length() > 0) {
            double newMaxDist = Double.parseDouble(maxDistEditText.getText().toString());
            bssidMatcher.setMAX_DISTANCE_DIFFERENCE(newMaxDist);
            maxDistEditText.setText(null);
        }
        if (maxTimeEditText.getText().toString().length() > 0) {
            double newMaxTime = Double.parseDouble(maxTimeEditText.getText().toString());
            bssidMatcher.setMAX_TIME_DIFFERENCE(newMaxTime);
            maxTimeEditText.setText(null);
        }
        if (minHotspotsEditText.getText().toString().length() > 0) {
            double newMinHotspots = Double.parseDouble(minHotspotsEditText.getText().toString());
            bssidMatcher.setMIN_NUMBER_OF_NEAR_HOTSPOTS(newMinHotspots);
            minHotspotsEditText.setText(null);
        }
        if (minTimestampsEditText.getText().toString().length() > 0) {
            double newMinTimestamps = Double.parseDouble(minTimestampsEditText.getText().toString());
            bssidMatcher.setMIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS(newMinTimestamps);
            minTimestampsEditText.setText(null);
        }
        initialiseSettings();
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(), 0);
    }

    public void getMatchingBSSIDs(View view) {
        bssidMatcher.getMatchingBSSIDs();
    }

    public void deleteData(View view) {
        databaseManager.deleteData();
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText("Data Deleted");

    }

    public void uploadScans(View view) {
        Log.d("debug", "Uploading scans");
        TextView resultTextView = findViewById(R.id.resultTextView);

        List<String[]> results = databaseManager.getRawScanData();
        JSONObject jsonBody = new JSONObject();
        List<JSONObject> scans = null;
        JSONArray locationData = databaseManager.getRawLocationData();

        try {
            scans = Scan.mapScansToJSON(results);
            jsonBody.put("scans", new JSONArray(scans));
            jsonBody.put("wifis",locationData);

        } catch (JSONException e) {
            resultTextView.setText("There was an error." + e.getMessage());
            return;
        }

        String URL = VolleySingleton.API_URL + "scans/insert/new";
        sendPOST(URL, jsonBody);

        StringBuilder out = new StringBuilder();
        List<Scan> uploadedScans = null;
        try {
            uploadedScans = bssidMatcher.parseScans(new JSONArray(scans));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        uploadedScans.sort(Comparator.comparing(Scan::getTimestamp));
        for (Scan scan : uploadedScans) {
            out.append("\n").append(scan.toString());
        }
        resultTextView.setText("Uploading " + results.size() + " scans\n" + out.toString());
    }

    public void deleteCollection(View view) {
        TextView resultTextView = findViewById(R.id.resultTextView);

        List<String[]> results = databaseManager.getRawScanData();
        JSONObject jsonBody = new JSONObject();
        List<JSONObject> scans = null;
        try {
            scans = Scan.mapScansToJSON(results);
            jsonBody.put("scans", new JSONArray(scans));
        } catch (JSONException e) {
            resultTextView.setText("There was an error." + e.getMessage());
            return;
        }

        String URL = VolleySingleton.API_URL + "scans/drop/all";
        sendPOST(URL, null);

        resultTextView.setText("Deleted scans.");
    }

    private void sendPOST(String URL, JSONObject jsonBody) {
        try {
            CustomJsonArrayRequest customJsonArrayRequest = new CustomJsonArrayRequest(Request.Method.POST, URL, jsonBody, this::response, Throwable::printStackTrace);
            VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().add(customJsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void response(JSONArray jsonArray) {
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText("Uploaded!");
    }

    public void closeActivity(View view) {
        finish();
    }
}