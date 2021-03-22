package com.prj.app;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    public static String API_URL = "http://3.8.139.11:4683/api/v1/";
    private static VolleySingleton instance;
    private final RequestQueue requestQueue;

    private VolleySingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public static void setAPI_URL(String newUrl) {
        API_URL = String.format("http://%s/api/v1/", newUrl);

    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}