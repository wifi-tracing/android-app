package com.prj.app;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationManager {
    private static final String CHANNEL_ID = "ExposureNotification";

    /**
     * Show a notification of possible exposure to SARS-CoV-2
     * @param context the context from which to send the notification
     */
    public static void showExposureNotification(Context context){
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_coronavirus)
                .setContentTitle("You are at risk of exposure!")
                .setContentText("We have found a possible exposure to SARS-CoV-2. Please contact your GP to get tested as soon as possible.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("We have found a possible exposure to SARS-CoV-2! Please contact your GP to get tested as soon as possible."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(1, builder.build());
    }

    private static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Exposure Notification";
            String description = "Show notification for exposure to SARS-CoV-2";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
