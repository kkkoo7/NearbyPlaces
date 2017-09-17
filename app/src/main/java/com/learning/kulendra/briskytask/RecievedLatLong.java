package com.learning.kulendra.briskytask;

/**
 * Created by kulendra on 17/9/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;



/**
 * Created by kulendra on 17/9/17.
 */

public class RecievedLatLong extends BroadcastReceiver {
    String TAG="RecievedLatLong";
    protected static double Latitude;
    protected static double Longitude;

    private ArrayList<String> messg;
    @Override
    public void onReceive(Context context, Intent intent) {
        messg=intent.getStringArrayListExtra("EXTRA");
        Latitude=Double.parseDouble(messg.get(0));
        Longitude=Double.parseDouble(messg.get(1));
        Log.d(TAG,"Lat: "+Latitude+" Long:"+Longitude);
    }

}

