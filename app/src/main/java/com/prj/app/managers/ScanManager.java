package com.prj.app.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public class ScanManager {
    private static WifiManager wifiManager;
    private final DatabaseManager databaseManager;
    private Location location;

    public ScanManager(Context context) {

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
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        databaseManager = DatabaseManager.getInstance(context.getApplicationContext());
    }

    public void startScan(Location location) {
        this.location = location; //update latest location
        boolean result = wifiManager.startScan(); //true if scan was successful
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        databaseManager.addScan(results);
        if (location != null) {
            databaseManager.addLocationData(results, location);
        }
    }


    private void scanFailure() {

    }


}
