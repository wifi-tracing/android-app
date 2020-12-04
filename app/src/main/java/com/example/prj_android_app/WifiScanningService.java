package com.example.prj_android_app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class WifiScanningService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int DELAY_MILLIS = 30001; //only once every 30s as for Android 9+ (four times in a 2 minute period)
    private static HandlerThread handlerThread;
    @SuppressLint("StaticFieldLeak")
    private static WifiScanner wifiScanner;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Wifi Contact Tracing enabled")
                .setContentText("Exposure notifications are active")
                .setSmallIcon(R.drawable.ic_coronavirus)
                .setContentIntent(pendingIntent)
                .build();
        startForeground((int) System.currentTimeMillis(), notification);

        handlerThread = new HandlerThread("WifiScanningThread");
        handlerThread.setDaemon(true);
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        if (wifiScanner == null) {
            wifiScanner = new WifiScanner(getApplicationContext());
        }

        getScanWifiRunnable(handler).run();
        return START_STICKY;
    }

    private Runnable getScanWifiRunnable(Handler handler) {

        return new Runnable() {
            @Override
            public void run() {
                wifiScanner.startScan();
                handler.postDelayed(this, DELAY_MILLIS);
            }
        };
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
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}