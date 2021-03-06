package com.prj.app;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Util {
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
