package com.prj.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class WifiScanningService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int DELAY_MILLIS = 3000; //only once every 30s as for Android 9+ (four times in a 2 minute period)
    private static final int MINIMUM_ACCURACY = 50; //accuracy in meters needed to log location data
    private static HandlerThread handlerThread;
    private static Notification notification;
    @SuppressLint("StaticFieldLeak")
    private static WifiScanner wifiScanner;
    public Location previousBestLocation = null;
    public LocationManager locationManager;
    public Intent intent;
    public CustomLocationListener listener;
    private DatabaseManager databaseManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        databaseManager = new DatabaseManager(getApplicationContext());
        if (notification == null) {
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Wifi Contact Tracing enabled")
                    .setContentText("Exposure notifications are active")
                    .setSmallIcon(R.drawable.ic_coronavirus)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        startForeground((int) System.currentTimeMillis(), notification);
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

                if (databaseManager.canSaveHotspotLocation()) {
                    if (locationManager == null) {
                        initialiseLocationListener();
                        Log.d("wifi", "Registered location listener");
                    }
                } else {
                    if (locationManager != null && listener != null) {
                        locationManager.removeUpdates(listener);
                        locationManager = null;
                        listener = null;
                        previousBestLocation = null;
                        Log.d("wifi", "Turned off location logging.");
                    }
                }

                handler.postDelayed(this, DELAY_MILLIS);
            }
        };
    }

    private void initialiseLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new CustomLocationListener(this, MINIMUM_ACCURACY, DELAY_MILLIS);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DELAY_MILLIS, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DELAY_MILLIS, 0, listener);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


}