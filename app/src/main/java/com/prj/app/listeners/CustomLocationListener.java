package com.prj.app.listeners;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.prj.app.managers.NotificationManager;
import com.prj.app.managers.PreferencesManager;
import com.prj.app.services.WifiScanningService;
import com.prj.app.ui.MainActivity;

import org.jetbrains.annotations.NotNull;

public class CustomLocationListener implements LocationListener {
    private final WifiScanningService wifiScanningService;
    private final int minimumAccuracy;
    private final NotificationManager notificationManager;
    private final Context context;
    private final int delayMillis;

    /**
     * Location listener that updates the wifiScanningService object populating its previousBestLocation property
     *
     * @param wifiScanningService the wifiScanningService object
     * @param minimumAccuracy     the minimum accuracy (in meters) needed for a location scan to be registered
     * @param delayMillis         the minimum time gap (in milliseconds) between location scans for a scan to be registered
     */
    public CustomLocationListener(@NotNull WifiScanningService wifiScanningService, int minimumAccuracy, int delayMillis, @NotNull Context context) {
        this.wifiScanningService = wifiScanningService;
        this.minimumAccuracy = minimumAccuracy;
        this.delayMillis = delayMillis;
        this.context = context;
        notificationManager = NotificationManager.getInstance(context);
    }

    public void onLocationChanged(final Location loc) {
        if (PreferencesManager.getInstance(context).canLogLocation()) {
            if (isBetterLocation(loc, wifiScanningService.previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                Log.d("wifi", "New Location: " + loc.getLatitude() + ", " + loc.getLongitude() + ", accuracy: " + loc.getAccuracy());
                wifiScanningService.previousBestLocation = loc;
            } else {
                wifiScanningService.previousBestLocation = null;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderDisabled(String provider) {
        notificationManager.setIsScanningVisible(false);
        notificationManager.updateScanningNotification();
        MainActivity.updateScanningUI(context);
    }

    public void onProviderEnabled(String provider) {
        notificationManager.setIsScanningVisible(true);
        notificationManager.updateScanningNotification();
        MainActivity.updateScanningUI(context);
    }

    /**
     * Check if a location object is better then another. It compares delay between the locations and
     * accuracy in order to decide.
     *
     * @param location            the new location to compare
     * @param currentBestLocation the location to compare it to
     * @return true if location is better then currentBestLocation
     * @see <a href="https://stackoverflow.com/a/6280851">Stack Overflow</a>
     */
    private boolean isBetterLocation(@NotNull Location location, Location currentBestLocation) {
        if (location.getAccuracy() > minimumAccuracy) {
            return false;
        }
        if (currentBestLocation == null) {
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > delayMillis;
        boolean isSignificantlyOlder = timeDelta < -delayMillis;
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
}
