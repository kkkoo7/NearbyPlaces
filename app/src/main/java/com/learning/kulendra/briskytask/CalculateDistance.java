package com.learning.kulendra.briskytask;

import android.util.Log;

/**
 * Created by kulendra on 17/9/17.
 */

public class CalculateDistance {
    private double lat1;
    private double lat2;
    private double lon1;
    private double lon2;

    private final String TAG="CalculateDistance";

    public CalculateDistance(double lat1, double lon1, double lat2, double lon2){
        this.lat1=lat1;
        this.lat2=lat2;
        this.lon1=lon1;
        this.lon2=lon2;
        Log.d(TAG,"Constructor called");
    }
    protected double distance() {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        Log.d(TAG,"distance is "+dist);
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
