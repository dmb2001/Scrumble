package com.app.scrumble.model;

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

    private double radians(double x){
        return x * PIx / 180;
    }

    public double distanceBetween(
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
     * distance in miles between the two locations
     * @param location
     * @return
     */
    public int distanceFrom(Location location){
        Objects.requireNonNull(location);
        return BigDecimal.valueOf(distanceBetween(this.longitude, this.latitude, location.getLongitude(), location.getLatitude())*0.621371).intValue();
    }

}
