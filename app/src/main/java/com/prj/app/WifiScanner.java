package com.prj.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class WifiScanner {
    private static WifiManager wifiManager;
    private final Context context;
    private final DatabaseManager databaseManager;
    private List<ScanResult> results;
    private Location location;
    private boolean success;

    public WifiScanner(Context context) {
        this.context = context;

        wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        databaseManager = new DatabaseManager(context);
    }

    public void startScan(Location location) {
        this.location = location; //update latest location
        boolean result = wifiManager.startScan(); //true if scan was successful
        Log.d("wifi", "startScan() " + result);
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    private void scanSuccess() {
        results = wifiManager.getScanResults();
        databaseManager.addScan(results);
        if (location != null) {
            databaseManager.addLocationData(results, location);
        }
        Log.d("wifi", "Found " + results.size() + " results");
    }


    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        Log.d("wifi", "Failed scan");
    }


}
