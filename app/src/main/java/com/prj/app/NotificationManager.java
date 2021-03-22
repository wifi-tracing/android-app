package com.prj.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.NotNull;

@SuppressLint("StaticFieldLeak")
public class NotificationManager {
    private static final String EXPOSURE_CHANNEL_ID = "ExposureNotification";
    private static final String LOCATION_CHANNEL_ID = "LocationAlerts";
    private static final String SCANNING_CHANNEL_ID = "ScanningNotification";
    private static final int EXPOSURE_NOTIFICATION_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 10;
    private static NotificationManager instance;
    private static Context context;
    private static NotificationManagerCompat notificationManager;
    private boolean isScanningVisible;

    public NotificationManager(@NotNull Context context) {
        NotificationManager.context = context;
        notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel(context, "Exposure Notification", "Show notification for exposure to SARS-CoV-2", EXPOSURE_CHANNEL_ID);
        createNotificationChannel(context, "Location Alerts", "Alert User when Location Services are turned off.", LOCATION_CHANNEL_ID);
        createNotificationChannel(context, "Scanning Notifications", "Show User the status of WiFi scanning", SCANNING_CHANNEL_ID);
    }

    public static NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }

    private static void createNotificationChannel(Context context, String name, String description, String channelID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public boolean getIsScanningVisible() {
        return isScanningVisible;
    }

    public void setIsScanningVisible(boolean isScanningVisible) {
        this.isScanningVisible = isScanningVisible;
    }

    /**
     * Show a notification of possible exposure to SARS-CoV-2
     */
    public void showExposureNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EXPOSURE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_coronavirus)
                .setContentTitle("You are at risk of exposure!")
                .setContentText("We have found a possible exposure to SARS-CoV-2. Please contact your GP to get tested as soon as possible.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("We have found a possible exposure to SARS-CoV-2! Please contact your GP to get tested as soon as possible."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        notificationManager.notify(EXPOSURE_NOTIFICATION_ID, builder.build());
    }


    public Notification getScanningNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);
        if (isScanningVisible) {
            return new NotificationCompat.Builder(context, SCANNING_CHANNEL_ID)
                    .setContentTitle("Wifi Contact Tracing enabled")
                    .setContentText("Exposure notifications are active")
                    .setSmallIcon(R.drawable.ic_coronavirus)
                    .setContentIntent(pendingIntent)
                    .setShowWhen(false)
                    .build();
        } else {
            return new NotificationCompat.Builder(context, SCANNING_CHANNEL_ID)
                    .setContentTitle("Wifi Contact Tracing is disabled!")
                    .setContentText("Exposure notifications are not active. Enable Location and WiFi services to restore them.")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Exposure notifications are not active. Enable Location and WiFi services to restore them."))
                    .setSmallIcon(R.drawable.ic_coronavirus)
                    .setContentIntent(pendingIntent)
                    .setShowWhen(false)
                    .build();
        }
    }

    public void updateScanningNotification() {
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, getScanningNotification());
    }


}
