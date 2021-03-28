package com.prj.app.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.prj.app.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}