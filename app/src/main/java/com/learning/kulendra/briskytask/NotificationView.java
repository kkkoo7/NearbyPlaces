package com.learning.kulendra.briskytask;

/**
 * Created by kulendra on 16/9/17.
 */

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import java.util.ArrayList;

public class NotificationView extends Activity{
    private ArrayList<String> messg;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        TextView textView=findViewById(R.id.notif);
        messg=getIntent().getStringArrayListExtra("EXTRA");
        textView.setText(messg.get(0)+" is near you with\n"+messg.get(1)+"\ndistance "+messg.get(2));
    }
}
