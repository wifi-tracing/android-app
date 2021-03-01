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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n", "SimpleDateFormat"})
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
        Switch switchView = findViewById(R.id.locationSwitch);
        boolean newValue = !databaseManager.canSaveHotspotLocation();
        databaseManager.setSaveHotspotLocation(newValue);
        switchView.setChecked(newValue);
    }

    private void initialiseSettings() {
        Switch switchView = findViewById(R.id.locationSwitch);
        switchView.setChecked(databaseManager.canSaveHotspotLocation());
    }

    public void getMatchingBSSIDs(View view) {
        BSSIDMatcher BSSIDMatcher = new BSSIDMatcher(databaseManager,
                findViewById(R.id.resultTextView),
                this.getApplicationContext());
        BSSIDMatcher.getMatchingBSSIDs();
    }


    public void uploadScans(View view) {
        Log.d("debug", "Uploading scans");
        TextView resultTextView = findViewById(R.id.resultTextView);

        List<String[]> results = databaseManager.getRawScanData();
        JSONObject jsonBody = new JSONObject();
        List<JSONObject> scans = null;
        try {
            scans = mapScansToJSON(results);
            jsonBody.put("scans", new JSONArray(scans));
        } catch (JSONException e) {
            resultTextView.setText("There was an error." + e.getMessage());
            return;
        }

        resultTextView.setText("Checking matching BSSIDs");

        String URL = VolleySingleton.API_URL + "scans/insert/new";
        sendUploadScansPOST(URL, jsonBody);

        resultTextView.setText("Uploading scans");
    }

    private List<JSONObject> mapScansToJSON(List<String[]> scans) throws JSONException {
        List<JSONObject> result = new ArrayList<>();
        for (String[] scan : scans) {
            JSONObject object = new JSONObject();
            object.put("d", "false");
            object.put("t", scan[2]);
            object.put("l", Double.parseDouble(scan[1]));
            object.put("b", scan[0]);
            result.add(object);
        }
        return result;
    }

    private void sendUploadScansPOST(String URL, JSONObject jsonBody) {
        try {
            CustomJsonArrayRequest customJsonArrayRequest = new CustomJsonArrayRequest(Request.Method.POST, URL, jsonBody, this::response, Throwable::printStackTrace);
            VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().add(customJsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void response(JSONArray jsonArray) {
    }

    public void closeActivity(View view) {
        finish();
    }
}