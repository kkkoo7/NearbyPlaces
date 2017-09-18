package com.learning.kulendra.briskytask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kulendra on 17/9/17.
 */

public class SendNotification extends Service{
    private String NotificationTitle;
    private String NotificationText;
    private ArrayList<String> NotificationArray=new ArrayList<>();;
    private final String TAG="SendNotification";
    private NotificationManager manager;
    private double LatitudeS;
    private double LongitudeS;
    private double LatitudeL;
    private double LongitudeL;
    private RecievedLatLong R;
    private CalculateDistance cd;
    private double distance;


    @Override
    public void onCreate(){

    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        NotificationArray=intent.getStringArrayListExtra("EXTRA");
        LatitudeS=Double.parseDouble(NotificationArray.get(5));
        LongitudeS=Double.parseDouble(NotificationArray.get(6));
        R=new RecievedLatLong();
        LatitudeL=R.Latitude;
        LongitudeL=R.Longitude;
        cd=new CalculateDistance(LatitudeL,LongitudeL,LatitudeS,LongitudeS);
        distance=cd.distance();
        if(distance<1) {
            NotificationTitle = NotificationArray.get(0);
            NotificationText = NotificationArray.get(0) + " is near you";
            sendNotification();
        }
        Log.d(TAG,"accessing Lat: "+LatitudeL+" "+LongitudeL);
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy(){

    }


    public void sendNotification(){
        addNotification(NotificationArray);
    }
    private void addNotification(ArrayList<String> RecievedPOS) {
        Log.d(TAG,"addNotification called");
        String channel="ChannelID1";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),channel)
                        .setSmallIcon(com.learning.kulendra.briskytask.R.drawable.abc)
                        .setContentTitle(NotificationTitle)
                        .setContentText(NotificationText)
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(getApplicationContext(), NotificationView.class);
        notificationIntent.putStringArrayListExtra("EXTRA",RecievedPOS);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
