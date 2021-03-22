package com.prj.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

@SuppressLint("StaticFieldLeak")
public class WifiScanningService extends Service {
    private static final int DELAY_MILLIS = 30001; //only once every 30s as for Android 9+ (four times in a 2 minute period)
    private static final int MINIMUM_ACCURACY = 50; //accuracy in meters needed to log location data
    private static final int FOREGROUND_ID = 10;
    private static HandlerThread handlerThread;
    private static WifiScanner wifiScanner;
    public Location previousBestLocation = null;
    public LocationManager locationManager;
    public Intent intent;
    public CustomLocationListener locationListener;
    private CustomWifiListener wifiListener;

    @Override
    public void onCreate() {
        super.onCreate();
        registerLocationListener();
        registerWiFiListener();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;

        Notification notification = NotificationManager.getInstance(getApplicationContext()).getScanningNotification();
        startForeground(FOREGROUND_ID, notification);

        if (handlerThread == null) {
            handlerThread = new HandlerThread("WifiScanningThread");
            handlerThread.setDaemon(true);
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            if (wifiScanner == null) {
                wifiScanner = new WifiScanner(getApplicationContext());
            }

            getScanWifiRunnable(handler).run();
            return START_NOT_STICKY;
        }
        return START_NOT_STICKY;
    }


    private Runnable getScanWifiRunnable(Handler handler) {
        return new Runnable() {
            @Override
            public void run() {
                wifiScanner.startScan(previousBestLocation);
                if (!DatabaseManager.getInstance(getApplicationContext()).canSaveHotspotLocation()) {
                    previousBestLocation = null;
                }
                handler.postDelayed(this, DELAY_MILLIS);
            }
        };
    }


    /**
     * Register a wifi state listener. Used to check the status of wifi services.
     */
    private void registerWiFiListener() {
        if (wifiListener == null) {
            wifiListener = new CustomWifiListener();
            getApplicationContext().registerReceiver(wifiListener, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }

    }

    /**
     * Register a location listener. Used to check status of location services, and to fetch location data if enabled
     */
    private void registerLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener(this, MINIMUM_ACCURACY, DELAY_MILLIS, getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DELAY_MILLIS, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DELAY_MILLIS, 0, locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}