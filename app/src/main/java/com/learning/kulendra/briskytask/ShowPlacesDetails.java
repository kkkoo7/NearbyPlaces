package com.learning.kulendra.briskytask;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import static com.learning.kulendra.briskytask.R.id.save;

public class ShowPlacesDetails extends AppCompatActivity {

    private ArrayList<String> messg;
    private String TAG="ShowPlacesDetails";
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_places_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView name=findViewById(R.id.setName);
        TextView open=findViewById(R.id.setOpen);
        TextView rating=findViewById(R.id.setRating);
        TextView vicinity=findViewById(R.id.setVicinity);
        TextView category=findViewById(R.id.setCategory);
        save=(Button) findViewById(R.id.save);
        getWindow().getDecorView().setBackgroundColor(Color.CYAN);
        messg=getIntent().getStringArrayListExtra("EXTRA");
        name.setText(messg.get(0));
        category.setText(messg.get(1));
        rating.setText(messg.get(2));
        open.setText(messg.get(3));
        vicinity.setText(messg.get(4));
        Log.d(TAG,"onCreate finished");
    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG,"onStart called");
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"onResume");
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("EXTRA",messg);
                intent.setAction("com.brisky.SAVEDPLACE_INTENT");
                sendBroadcast(intent);
                Log.d(TAG,"Broadcasted to StartNotificationService");
            }
        });
    }
    @Override
    protected void onPause(){
        Log.d(TAG,"onPause() called");
        super.onPause();
    }
    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG,"onStop()called");
    }

}
