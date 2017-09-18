package com.learning.kulendra.briskytask;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnConnectionFailedListener {
    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    protected Location mLastLocation;

    protected Boolean mRequestingLocationUpdates;
    private double mLatitudeLabel;
    private double mLongitudeLabel;

    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected EditText mEditTextView;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";

    private ArrayList<String> MyData;
    private LatLng saved;
    private String savedname;
    private double radius=500;
    private Map<Marker, ArrayList<String>> map=new HashMap<Marker,ArrayList<String>>();
    LatLng home;

    FindPlaces f;
    CalculateDistance cd;
    SendNotification sn;
    RecievedLatLong pos=new RecievedLatLong();
    private Marker marked;
    int flag=0;

    Location dummy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude);
        mEditTextView = (EditText) findViewById(R.id.radius);
        mEditTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mEditTextView.setFocusableInTouchMode(true);

                return false;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MapsActivity.this.startService(new Intent(this,GetUpdatedLocation .class));
        mRequestingLocationUpdates = false;
        dummy= new Location("dummy");
        dummy.setLatitude(17.3850);
        dummy.setLongitude(78.4867);
        mLastLocation=dummy;
        f=new FindPlaces(dummy,radius);
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Entered onStart");
        if (!checkPermissions()) {
            Log.d(TAG,"checkPermission returned false");
            startLocationPermissionRequest();
        } else {
            Log.d(TAG,"Permission was already granted");
        }
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Permission Granted");

                } else {
                    Log.i(TAG, "Permission Denied.");
                }
                return;
            }
        }
    }


    private void moveMap(Location loc) {
        if(loc.getLatitude()!=0&&loc.getLongitude()!=0) {
            Log.d(TAG,"moveMap Called with non zero latlong:"+loc.getLatitude()+" "+loc.getLongitude());
            mLatitudeLabel = loc.getLatitude();
            mLongitudeLabel = loc.getLongitude();
            LatLng latLng = new LatLng(mLatitudeLabel, mLongitudeLabel);

            if (flag == 0) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .title("Home Location"));
                flag++;
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mLatitudeTextView.setText("" + mLatitudeLabel);
            mLongitudeTextView.setText("" + mLongitudeLabel);
        }
    }

    private void mark(Location loc,ArrayList<String>MyData){
        LatLng l= new LatLng(loc.getLatitude(),loc.getLongitude());
        mMap.addCircle(new CircleOptions().center(l).radius(20).fillColor(Color.BLUE).strokeColor(Color.RED));
        Marker s1=mMap.addMarker(new MarkerOptions().position(l).draggable(true).title(MyData.get(0)));
        map.put(s1,MyData);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                if(arg0 != null){
                    Intent intent = new Intent(MapsActivity.this,ShowPlacesDetails.class);
                    intent.putStringArrayListExtra("EXTRA",map.get(arg0));
                    MapsActivity.this.startActivity(intent);
                    return true;
                }
                return  false;
            }

        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Hyderabad and move the camera
        LatLng brisky = new LatLng(17.3850,78.4867);
        mMap.addMarker(new MarkerOptions().position(brisky).title("Brisky"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(brisky));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            mLastLocation.setLatitude(pos.Latitude);
            mLastLocation.setLongitude(pos.Longitude);
            moveMap(mLastLocation);
            //Location last=mLastLocation;
            f.getExecute();
            for (int i=0;i<f.g1.size();i++)
            {
                MyData=new ArrayList<>();
                MyData.add(f.g1.get(i).getName());
                MyData.add(f.g1.get(i).getCategory());
                MyData.add(f.g1.get(i).getRating());
                MyData.add(f.g1.get(i).getOpenNow());
                MyData.add(f.g1.get(i).getVicinity());
                Location near=f.g1.get(i).getL();
                MyData.add(""+near.getLatitude());
                MyData.add(""+near.getLongitude());
                Log.d(TAG,"lat"+near.getLatitude()+"long"+near.getLongitude());
                mark(near,MyData);
            }
        }
    }

    public void stopUpdatesButtonHandler(View view) {
        if (mEditTextView.getText().toString() != null) {
            radius = Double.parseDouble(mEditTextView.getText().toString());
            f.getExecuteInstance(radius, mLastLocation);
            if (mRequestingLocationUpdates) {
                mRequestingLocationUpdates = false;
                setButtonsEnabledState();
                mMap.clear();
                map.clear();
                home = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(home)
                        .draggable(true)
                        .title("Home Location"));
            }
            mEditTextView.setFocusable(false);
        }
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }



    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
    }

    @Override
    protected void onResume() {
        super.onResume();

            mLastLocation.setLatitude(pos.Latitude);
            mLastLocation.setLongitude(pos.Longitude);
            Log.d(TAG,"In Resume:"+pos.Latitude+" "+pos.Longitude);
           // moveMap(mLastLocation);
        Log.d(TAG,"Venues"+f.g1.size());
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onStop(){
        super.onStop();
        mLastLocation.setLatitude(pos.Latitude);
        mLastLocation.setLongitude(pos.Longitude);
        Log.d(TAG,"onStop");
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.d(TAG,"onRestart");
        mLastLocation.setLatitude(pos.Latitude);
        mLastLocation.setLongitude(pos.Longitude);
        moveMap(mLastLocation);
    }



    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
        // ...
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            //if(mLastLocation!=null)
                moveMap(mLastLocation);
        }
    }

    @Override
    public void onConnectionFailed (ConnectionResult result){
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }
}
