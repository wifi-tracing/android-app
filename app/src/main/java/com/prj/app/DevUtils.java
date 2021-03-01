package com.prj.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        resultTextView.setText("Uploading scans");
    }

    public void closeActivity(View view) {
        finish();
    }
}