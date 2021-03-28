package com.prj.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.prj.app.R;

import java.util.Objects;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_preferences);
    }
}