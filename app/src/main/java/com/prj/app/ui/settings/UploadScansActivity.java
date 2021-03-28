package com.prj.app.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.prj.app.R;
import com.prj.app.api.CustomJsonArrayRequest;
import com.prj.app.api.VolleySingleton;
import com.prj.app.logic.BSSIDMatcher;
import com.prj.app.managers.DatabaseManager;
import com.prj.app.managers.PreferencesManager;
import com.prj.app.util.Scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class UploadScansActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private BSSIDMatcher bssidMatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_scans);
        Objects.requireNonNull(getSupportActionBar()).hide();

        databaseManager = DatabaseManager.getInstance(getApplicationContext());
        bssidMatcher = new BSSIDMatcher(databaseManager,
                findViewById(R.id.resultTextView),
                this.getApplicationContext());

        refreshStats(null);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText("");
    }

    public void refreshStats(View view) {
        String scansCount = String.valueOf(databaseManager.getScansCount());
        String apScansCount = String.valueOf(databaseManager.getAPScansCounts());
        TextView scansCountContent = findViewById(R.id.numberOfScansContent);
        TextView apScansCountContent = findViewById(R.id.numberOfAPScansContent);

        scansCountContent.setText(scansCount);
        apScansCountContent.setText(apScansCount);
    }


    public void uploadScans(View view) {
        TextView resultTextView = findViewById(R.id.resultTextView);

        List<String[]> results = databaseManager.getRawScanData();
        JSONObject jsonBody = new JSONObject();
        List<JSONObject> scans = new ArrayList<>();

        boolean canUploadLocation = ((SwitchCompat) findViewById(R.id.uploadLocationSwitch)).isChecked();
        JSONArray locationData = canUploadLocation ? databaseManager.getRawLocationData() : new JSONArray();

        try {
            scans = results.size() > 0 ? Scan.mapScansToJSON(results) : scans;
            jsonBody.put("scans", new JSONArray(scans));
            jsonBody.put("wifis", locationData);
            jsonBody.put("token", "Jt(I9}SFd~|.}c^ZN?(4y8m?aI0~-b");
        } catch (JSONException e) {
            resultTextView.setText("There was an error." + e.getMessage());
            return;
        }

        String URL = VolleySingleton.getApiUrl() + "scans/insert/new";
        sendPOST(URL, jsonBody);

        StringBuilder out = new StringBuilder();
        List<Scan> uploadedScans = null;
        try {
            uploadedScans = bssidMatcher.parseScans(new JSONArray(scans));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert uploadedScans != null;
        uploadedScans.sort(Comparator.comparing(Scan::getTimestamp));
        for (Scan scan : uploadedScans) {
            out.append("\n").append(scan.toString());
        }
        resultTextView.setText("Uploading " + results.size() + " scans\n" + out.toString());
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
}