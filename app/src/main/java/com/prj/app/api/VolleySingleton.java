package com.prj.app.api;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.prj.app.managers.PreferencesManager;

@SuppressLint("StaticFieldLeak")
public class VolleySingleton {
    private static VolleySingleton instance;
    private final RequestQueue requestQueue;
    private static Context context;

    private VolleySingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        VolleySingleton.context =  context;
    }

    public static VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public static String getApiUrl(){
        return String.format("http://%s/api/v1/", PreferencesManager.getInstance(context).getApiUrl());
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}