package com.prj.app;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CustomLocationListener implements LocationListener {
    private final WifiScanningService wifiScanningService;
    private final int minimumAccuracy;
    private final int delayMillis;

    /**
     * Location listener that updates the wifiScanningService object populating its previousBestLocation property
     *
     * @param wifiScanningService the wifiScanningService object
     * @param minimumAccuracy     the minimum accuracy (in meters) needed for a location scan to be registered
     * @param delayMillis         the minimum time gap (in milliseconds) between location scans for a scan to be registered
     */
    public CustomLocationListener(WifiScanningService wifiScanningService, int minimumAccuracy, int delayMillis) {
        this.wifiScanningService = wifiScanningService;
        this.minimumAccuracy = minimumAccuracy;
        this.delayMillis = delayMillis;
    }

    public void onLocationChanged(final Location loc) {
        Log.d("wifi", "Location changed");
        if (isBetterLocation(loc, wifiScanningService.previousBestLocation)) {
            loc.getLatitude();
            loc.getLongitude();
            Log.d("wifi", "New Location: " + loc.getLatitude() + ", " + loc.getLongitude() + ", accuracy: " + loc.getAccuracy());
            wifiScanningService.previousBestLocation = loc;
        } else {
            wifiScanningService.previousBestLocation = null;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderDisabled(String provider) {
        Toast.makeText(wifiScanningService.getApplicationContext(), "Gps Disabled. Location data will not be saved.", Toast.LENGTH_SHORT).show();
    }


    public void onProviderEnabled(String provider) {
        Toast.makeText(wifiScanningService.getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (location.getAccuracy() > minimumAccuracy) {
            Log.d("wifi", "Location accuracy was insufficient (" + location.getAccuracy() + ").");
            return false;
        }

        if (currentBestLocation == null) {
            // A new location is always better than no location
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
