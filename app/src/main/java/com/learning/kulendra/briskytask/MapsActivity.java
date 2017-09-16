package com.learning.kulendra.briskytask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;

import java.util.ArrayList;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    protected Location mLastLocation;
    protected LocationManager locationManager;

    protected Boolean mRequestingLocationUpdates;
    private double mLatitudeLabel;
    private double mLongitudeLabel;

    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected EditText mEditTextView;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;

    private LatLng saved;
    private String savedname;
    private double savedDist;
    private double radius=500;

    FindPlaces f;
    int flag=0;

    Location dummy;

    NotificationManager manager;

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
        checkGPSStatus();
        mRequestingLocationUpdates = false;

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient=Places.getPlaceDetectionClient(this,null);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mGoogleApiClient.connect();
        dummy= new Location("dummy");
        dummy.setLatitude(17.3850);
        dummy.setLongitude(78.4867);
        mLastLocation=dummy;
        f=new FindPlaces(dummy,radius);
        getUpdatedLocation();
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
            getUpdatedLocation();
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
                    getUpdatedLocation();

                } else {
                    Log.i(TAG, "Permission Denied.");
                }
                return;
            }
        }
    }



    private void moveMap(Location loc) {
        mLatitudeLabel=loc.getLatitude();
        mLongitudeLabel=loc.getLongitude();
        LatLng latLng = new LatLng(mLatitudeLabel, mLongitudeLabel);

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Home Location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mLatitudeTextView.setText(""+mLatitudeLabel);
        mLongitudeTextView.setText(""+mLongitudeLabel);
    }

    private void mark(Location loc,String name){
        LatLng l= new LatLng(loc.getLatitude(),loc.getLongitude());
        mMap.addCircle(new CircleOptions().center(l).radius(20).fillColor(Color.BLUE).strokeColor(Color.RED));
        Marker s1=mMap.addMarker(new MarkerOptions().position(l).draggable(true).title(name));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                if(arg0 != null){
                    // if marker  source is clicked
                    Toast.makeText(MapsActivity.this, "Saving "+arg0.getTitle(), Toast.LENGTH_SHORT).show();// display toast
                    saved=arg0.getPosition();
                    savedname=arg0.getTitle();
                    return true;
                }
                return  false;
            }

        });
    }

    private void checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if ( locationManager == null ) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex){}
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex){}
        if ( !gps_enabled && !network_enabled ){
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
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
            getUpdatedLocation();
            moveMap(mLastLocation);
            Location last=mLastLocation;
            f.getExecute();
            for (int i=0;i<f.g1.size();i++)
            {
                Location near=f.g1.get(i).getL();
                Log.d(TAG,"lat"+near.getLatitude()+"long"+near.getLongitude());
                mark(near,f.g1.get(i).getName());
            }
        }
    }

    public void stopUpdatesButtonHandler(View view) {
        radius=Double.parseDouble(mEditTextView.getText().toString());
        f.getExecuteInstance(radius,mLastLocation);
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            mMap.clear();
        }
        mEditTextView.setFocusable(false);
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

        if (mRequestingLocationUpdates) {
            getUpdatedLocation();
        }
        Log.d(TAG,"Venues"+f.g1.size());
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG,"onPause");
        //getUpdatedLocation();
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG,"onStop");
        getUpdatedLocation();
        if(saved!=null)
        {
            savedDist=distance(mLastLocation.getLatitude(),mLastLocation.getLongitude(),saved.latitude,saved.longitude);
            if(savedDist<5){
                sendNotification(savedDist);
            }
        }
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.d(TAG,"onRestart");
        moveMap(mLastLocation);
    }

    protected void getUpdatedLocation()
    {
            //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager= (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
            int pstate=ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            boolean gstate=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Criteria criteria = new Criteria();
            if(gstate&&(pstate==PackageManager.PERMISSION_GRANTED)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // TODO Auto-generated method stub
                    if(location!=null)
                    {
                        mLastLocation=location;
                        flag++;
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }
            });
        }
        if (locationManager != null&&flag==0) {
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //mLastLocation = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
        }
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
            moveMap(mLastLocation);
        }
    }

    @Override
    public void onConnectionFailed (ConnectionResult result){
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    public void sendNotification(double dist){
        ArrayList<String> not=new ArrayList<>();
        not.add(savedname);
        not.add("Lat: "+saved.latitude+"\nLong: "+saved.longitude);
        not.add(""+dist);
        addNotification(not);
    }
    private void addNotification(ArrayList<String> RecievedPOS) {
        String channel="ChannelID1";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,channel)
                        .setSmallIcon(R.drawable.abc)
                        .setContentTitle("Brisky Notification")
                        .setContentText(savedname+" is near you")
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, NotificationView.class);
        notificationIntent.putStringArrayListExtra("EXTRA",RecievedPOS);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
