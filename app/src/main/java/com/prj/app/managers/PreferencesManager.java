package com.prj.app.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.prj.app.R;

import java.util.Objects;

public class PreferencesManager {

    private static PreferencesManager instance;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    public  PreferencesManager(Context context){
        this.context = context;
        sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean canLogLocation(){
        return sharedPreferences.getBoolean(context.getString(R.string.can_log_location), false);
    }
    public String getApiUrl(){
        return sharedPreferences.getString(context.getString(R.string.rest_api_url), "192.168.1.138:4683");
    }
    public Double getTMax(){
        return Double.valueOf(sharedPreferences.getString(context.getString(R.string.T_max), "35.0"));
    }
    public Double getDMax(){
        return Double.valueOf(sharedPreferences.getString(context.getString(R.string.D_max), "2.0"));
    }
    public Integer getHMin(){
        return Integer.valueOf(sharedPreferences.getString(context.getString(R.string.H_min), "6"));
    }
    public Integer getCMin(){
        return Integer.valueOf(sharedPreferences.getString(context.getString(R.string.C_min), "4"));
    }

    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }
}
