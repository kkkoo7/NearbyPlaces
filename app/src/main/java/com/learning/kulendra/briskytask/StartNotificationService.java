package com.learning.kulendra.briskytask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kulendra on 18/9/17.
 */

public class StartNotificationService extends BroadcastReceiver{
    private ArrayList<String> messg=new ArrayList<>();
    private String TAG="StartNotifService";
    @Override
    public void onReceive(Context context, Intent intent) {
        messg=intent.getStringArrayListExtra("EXTRA");
        Intent mIntent=new Intent(context,SendNotification.class);
        mIntent.putStringArrayListExtra("EXTRA",messg);
        context.startService(mIntent);
        Log.d(TAG,"Broadcast Recieved");
        }
    }
