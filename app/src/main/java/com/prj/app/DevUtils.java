package com.prj.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n"})
public class DevUtils extends AppCompatActivity {
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_dev_utils);
        databaseManager = new DatabaseManager(this);

        initialiseSettings();
    }

    public void toggleLocationSwitch(View view) {
        Switch switchView = (Switch) findViewById(R.id.locationSwitch);
        boolean newValue = !databaseManager.canSaveHotspotLocation();
        databaseManager.setSaveHotspotLocation(newValue);
        switchView.setChecked(newValue);
    }

    private void initialiseSettings() {
        Switch switchView = (Switch) findViewById(R.id.locationSwitch);
        switchView.setChecked(databaseManager.canSaveHotspotLocation());
    }

    public void getMatchingBSSIDs(View view) {
        DatabaseManager databaseManager = new DatabaseManager(this);
        List<String> results = databaseManager.getScanBSSIDs();

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("BSSIDs", new JSONArray(results));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView resultTextView = (TextView) findViewById(R.id.resultTextView);
        resultTextView.setText("Getting matching BSSIDs");

        String URL = VolleySingleton.API_URL + "scans/get/matchBSSID";
        sendMatchingBSSIDsPOST(URL, jsonBody);
    }

    private void sendMatchingBSSIDsPOST(String URL, JSONObject jsonBody) {
        TextView resultTextView = (TextView) findViewById(R.id.resultTextView);
        try {
            long startTime = System.nanoTime();
            CustomJsonArrayRequest customJsonArrayRequest = new CustomJsonArrayRequest(
                    Request.Method.POST,
                    URL,
                    jsonBody,
                    response -> resultTextView.setText(
                            "Cross referenced 5,000,000 scans\n" +
                                    "Found " + response.length() + " results\n" +
                                    "Time: " + ((System.nanoTime() - startTime) / 1000000) + "ms\n\n"),
                    Throwable::printStackTrace);

            VolleySingleton.getInstance(this).getRequestQueue().add(customJsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadScans(View view) {
        Log.d("debug", "Uploading scans");
        TextView resultTextView = (TextView) findViewById(R.id.resultTextView);
        resultTextView.setText("Uploading scans");
    }

    public void closeActivity(View view) {
        finish();
    }
}