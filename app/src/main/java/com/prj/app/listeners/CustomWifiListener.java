package com.prj.app.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.prj.app.managers.NotificationManager;
import com.prj.app.ui.MainActivity;

public class CustomWifiListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = NotificationManager.getInstance(context);
        int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

        switch (extraWifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_DISABLING:
                notificationManager.setIsScanningVisible(false);
                notificationManager.updateScanningNotification();
                MainActivity.updateRippleAnimation(context);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
            case WifiManager.WIFI_STATE_ENABLING:
                notificationManager.setIsScanningVisible(true);
                notificationManager.updateScanningNotification();
                MainActivity.updateRippleAnimation(context);
                break;
            default:
                break;
        }

    }

}
