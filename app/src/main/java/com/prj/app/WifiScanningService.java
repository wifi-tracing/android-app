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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class WifiScanningService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int DELAY_MILLIS = 31000; //only once every 30s as for Android 9+ (four times in a 2 minute period)
    private static final int MINIMUM_ACCURACY = 50; //accuracy in meters needed to log location data
    private static HandlerThread handlerThread;
    private static Notification notification;
    @SuppressLint("StaticFieldLeak")
    private static WifiScanner wifiScanner;
    public Location previousBestLocation = null;
    public LocationManager locationManager;
    public Intent intent;
    public CustomLocationListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
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
            initialiseLocationListener();
            return START_NOT_STICKY;
        }
        return START_NOT_STICKY;
    }

    private Runnable getScanWifiRunnable(Handler handler) {
        return new Runnable() {
            @Override
            public void run() {
                wifiScanner.startScan(previousBestLocation);
                handler.postDelayed(this, DELAY_MILLIS);
            }
        };
    }

    private void initialiseLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new CustomLocationListener();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, (LocationListener) listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
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

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (location.getAccuracy() > MINIMUM_ACCURACY) {
            Log.d("wifi", "Location accuracy was insufficient (" + location.getAccuracy() + ").");
            return false;
        }

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > DELAY_MILLIS;
        boolean isSignificantlyOlder = timeDelta < -DELAY_MILLIS;
        boolean isNewer = timeDelta > 0;

        // If it's been more than 30s since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public class CustomLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            Log.d("wifi", "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                Log.d("wifi", "New Location: lat: " + loc.getLatitude() + " lon: " + loc.getLongitude() + ", accuracy: " + loc.getAccuracy());
                previousBestLocation = loc;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled. Location data will not be saved.", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }
    }

}