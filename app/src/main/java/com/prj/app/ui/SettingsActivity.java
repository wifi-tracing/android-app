package com.prj.app.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.prj.app.R;
import com.prj.app.ui.fragments.PreferencesActivity;
import com.prj.app.ui.settings.CheckExposureActivity;
import com.prj.app.ui.settings.ManageDataActivity;
import com.prj.app.ui.settings.UploadScansActivity;

import java.util.Objects;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n", "SimpleDateFormat"})
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_settings);
    }


    public void openPreferences(View view) {
        startActivity(new Intent(this, PreferencesActivity.class));
    }

    public void openUploadScans(View view) {
        startActivity(new Intent(this, UploadScansActivity.class));
    }

    public void openManageData(View view) {
        startActivity(new Intent(this, ManageDataActivity.class));
    }

    public void openCheckExposure(View view) {
        startActivity(new Intent(this, CheckExposureActivity.class));
    }

    public void closeActivity(View view) {
        finish();
    }
}