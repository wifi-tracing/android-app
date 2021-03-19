package com.prj.app;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Util {

    /**
     * Extrapolate Latitude and Longitude of the average between two poins that lieon a great-circle. This method uses the Haversine formula for the great-circle distance between two points.
     * As taken from https://stackoverflow.com/questions/6671183/calculate-the-center-point-of-multiple-latitude-longitude-coordinate-pairs?lq=1
     *
     * @param locations List of LatLng object corresponding to the locations to average
     * @return a LatLng object representing the average point of all input LatLng objects
     */
    public static LatLng getLocationAverage(List<LatLng> locations) {
        if (locations.size() == 1) {
            return locations.get(0);
        }

        double x = 0;
        double y = 0;
        double z = 0;

        for (LatLng geoCoordinate : locations) {
            double latitude = geoCoordinate.latitude * Math.PI / 180;
            double longitude = geoCoordinate.longitude * Math.PI / 180;

            x += Math.cos(latitude) * Math.cos(longitude);
            y += Math.cos(latitude) * Math.sin(longitude);
            z += Math.sin(latitude);
        }

        int total = locations.size();

        x = x / total;
        y = y / total;
        z = z / total;

        double centralLongitude = Math.atan2(y, x);
        double centralSquareRoot = Math.sqrt(x * x + y * y);
        double centralLatitude = Math.atan2(z, centralSquareRoot);

        return new LatLng(centralLatitude * 180 / Math.PI, centralLongitude * 180 / Math.PI);
    }

    public static Double round(Double val, int decimalPlaces) {
        return new BigDecimal(val.toString()).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }
}
