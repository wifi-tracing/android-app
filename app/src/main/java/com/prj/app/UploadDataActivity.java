package com.prj.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

@SuppressLint("SetTextI18n")

public class UploadDataActivity extends AppCompatActivity {
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Uri uri = getIntent().getData();
        token = uri.getQueryParameter("token");
    }

    public void uploadScans(View view) {
        toggleLoading();

        DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());

        List<String[]> results = databaseManager.getRawScanData();
        JSONObject jsonBody = new JSONObject();
        List<JSONObject> scans = null;
        try {
            scans = Scan.mapScansToJSON(results);
            jsonBody.put("scans", new JSONArray(scans));
            jsonBody.put("token", token);
        } catch (JSONException e) {
            return;
        }

        String URL = VolleySingleton.API_URL + "scans/insert/new";
        sendPOST(URL, jsonBody);
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
        toggleLoading();


        new AlertDialog.Builder(this)
                .setTitle("There was an error")
                .setMessage("Make sure you are scanning an up-to-date QR code").show();
    }

    private void toggleLoading(){
        ProgressBar loadingBar = findViewById(R.id.loadingBar);
        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setVisibility(confirmButton.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        loadingBar.setVisibility(loadingBar.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }

    private void response(JSONObject response) {
        toggleLoading();
        try {
            if (response.getString("message").equals("success")) {

                new AlertDialog.Builder(this)
                        .setTitle("You have uploaded your data.")
                        .setMessage("Your data will help reduce contagion and save lives.\nThank you")
                .setCancelable(false)
                 .setPositiveButton("OK", ((dialog, which) -> {finish();})).show();

             return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new AlertDialog.Builder(getApplicationContext())
                .setTitle("There was an error")
                .setMessage("Could not parse the server's response").show();

    }
}