package com.prj.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class DevUtils extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_dev_utils);
    }

    @SuppressLint("SetTextI18n")
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
        RequestQueue requestQueue;
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024 * 1024); // 1MB cap

        BasicNetwork network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);

        requestQueue.start();
        String URL = "http://192.168.1.102:3000/api/v1/scans/matchBSSID";
        sendPOST(URL, requestQueue, jsonBody);
    }

    private void sendPOST(String URL, RequestQueue requestQueue, JSONObject jsonBody) {
        TextView resultTextView = (TextView) findViewById(R.id.resultTextView);

        try {
            long startTime = System.nanoTime();
            @SuppressLint("SetTextI18n") CustomJsonArrayRequest customJsonArrayRequest = new CustomJsonArrayRequest(
                    Request.Method.POST,
                    URL,
                    jsonBody,
                    response -> resultTextView.setText(
                            "Cross referenced 5,000,000 scans\n" +
                                    "Found " + response.length() + " results\n" +
                                    "Time: " + ((System.nanoTime() - startTime) / 1000000) + "ms\n\n"),
                    Throwable::printStackTrace);
            customJsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(customJsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void uploadScans(View view) {
        Log.d("debug", "uploading scans");
        TextView resultTextView = (TextView) findViewById(R.id.resultTextView);
        resultTextView.setText("Uploading scans");
    }

    public void closeActivity(View view) {
        finish();
    }
}