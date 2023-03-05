package com.app.scrumble.model.group.scrapbook;

import java.math.BigDecimal;
import java.util.Objects;

public class Location {

    private static final double PIx = 3.141592653589793;
    private static final double RADIUS = 6378.16;

    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private static double radians(double x){
        return x * PIx / 180;
    }

    /**
     * Returns the distance, in km, between two sets of latitude/longitude
     * @param lon1
     * @param lat1
     * @param lon2
     * @param lat2
     * @return
     */
    public static double distanceBetween(
            double lon1,
            double lat1,
            double lon2,
            double lat2){
        double dlon = radians(lon2 - lon1);
        double dlat = radians(lat2 - lat1);

        double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2)) + Math.cos(radians(lat1)) * Math.cos(radians(lat2)) * (Math.sin(dlon / 2) * Math.sin(dlon / 2));
        double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return angle * RADIUS;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * distance in metres between the two locations
     * @param location
     * @return
     */
    public long distanceFrom(Location location){
        Objects.requireNonNull(location);
        return BigDecimal.valueOf(distanceBetween(this.longitude, this.latitude, location.getLongitude(), location.getLatitude())*1000).longValue();
    }

    /**
     * @param locationA
     * @param locationB
     * @return The distance in metres between the two locations
     */
    public static long distanceBetween(Location locationA, Location locationB){
        Objects.requireNonNull(locationA);
        Objects.requireNonNull(locationB);
        return BigDecimal.valueOf(distanceBetween(locationA.longitude, locationA.latitude, locationB.getLongitude(), locationB.getLatitude())*1000).longValue();
    }

}
