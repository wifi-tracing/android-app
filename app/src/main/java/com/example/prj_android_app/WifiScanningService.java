package com.example.prj_android_app;
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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class WifiScanningService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int DELAY_MILLIS = 30001; //only once every 30s as for Android 9+ (four times in a 2 minute period)
    private HandlerThread handlerThread;
    private Handler handler;
    @Override
    public void onCreate() {
        super.onCreate();        
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Wifi Contact Tracing enabled")
                .setContentText("Do not close the app")
                .setSmallIcon(R.drawable.ic_coronavirus)
                .setContentIntent(pendingIntent)
                .build();
        startForeground((int) System.currentTimeMillis(), notification);
        handlerThread = new HandlerThread("MyLocationThread");
        handlerThread.setDaemon(true);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.postDelayed(getScanWifiRunnable(handler) , 0);
            
        return START_STICKY;
    }

    private Runnable getScanWifiRunnable(Handler handler) {
      Runnable  scanWifi = new Runnable() {
            @Override
            public void run() {
                Log.d("wifi", "running scan");
                WifiScanner wifi = new WifiScanner(getApplicationContext());
                wifi.startScan();
                handler.postDelayed(this, DELAY_MILLIS);
            }
        };
        
        return scanWifi;
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