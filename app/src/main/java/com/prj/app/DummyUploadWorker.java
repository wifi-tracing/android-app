package com.prj.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DummyUploadWorker extends Worker {
    public DummyUploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public @NotNull Result doWork() {
        DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());
        List<String[]> results = databaseManager.getRawScanData(); //get the actual data in order to have consistent request sizes
        //create dummy data
        for (String[] row : results) {
            row[0] = "x0‑xx‑xx‑xx‑xx‑xx";
            row[1] = "1";
            row[2] = new Date().toString();
        }

        JSONObject jsonBody = new JSONObject();
        List<JSONObject> scans = null;
        try {
            scans = Scan.mapScansToJSON(results);
            jsonBody.put("scans", new JSONArray(scans));
            jsonBody.put("token", getRandomString());
            jsonBody.put("d", true);
            jsonBody.put("wifis", new JSONArray());
        } catch (JSONException ignored) {
        }

        String URL = VolleySingleton.API_URL + "scans/insert/new";
        sendPOST(URL, jsonBody);

        enqueueNewRequest();
        return Result.success();
    }

    private void enqueueNewRequest() {
        Random random = new Random();

        OneTimeWorkRequest.Builder myWorkBuilder =
                new OneTimeWorkRequest.Builder(DummyUploadWorker.class).setInitialDelay(random.nextInt(7), TimeUnit.MINUTES); //between 0 and 128 minutes from now
        OneTimeWorkRequest uploadRequest = myWorkBuilder.build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork("DUMMY_UPLOAD", ExistingWorkPolicy.KEEP, uploadRequest);
    }

    private String getRandomString() {
        Random random = new Random();
        return String.valueOf(random.nextInt());
    }

    private void sendPOST(String URL, JSONObject jsonBody) {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, this::response, this::error);
            VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void error(VolleyError volleyError) {
    }

    private void response(JSONObject jsonObject) {
    }

}