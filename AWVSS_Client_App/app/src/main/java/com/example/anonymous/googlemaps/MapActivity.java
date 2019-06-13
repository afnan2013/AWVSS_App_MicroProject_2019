package com.example.anonymous.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_GRANTED_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    Button btn_start;
    //variables
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<LatLng> listpoints;
    protected LatLng currentLatLng=null;
    protected LatLng desnitation=null;
    protected LatLng boatLocation;
    protected boolean haveDestination = false;
    public String JSON_STRING = "";
    public String flag = "";
    private float lati;
    private float longi;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3C3F41")));
        btn_start = (Button)findViewById(R.id.btn_start);
        getLocationPermission();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        listpoints = new ArrayList<>();


        Bundle extras = getIntent().getExtras();
        String latitude = extras.getString("lat");
        String longitude = extras.getString("lon");
        boatLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        Log.d(TAG, "onCreate: Boat Location  "+ boatLocation);


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(desnitation != null) {
                    JsonParser jasonParser = new JsonParser();
                    jasonParser.execute();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        currentLatLng = getLocation();                          //Get My Location
        Log.d(TAG, "OnMapReady : Current Location : "+currentLatLng);
        if(currentLatLng == null){
            Toast.makeText(this, "Cannot find current location", Toast.LENGTH_SHORT).show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                                                                            != PackageManager.PERMISSION_GRANTED
                                                                                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                                                                            != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(boatLocation);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(markerOptions);


        Log.d(TAG, "onMapReady: Json Output : "+JSON_STRING);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                /*//reset marker when already 3
                listpoints.clear();
                mMap.clear();
                //save first point select
                listpoints.add(latLng);
                */
                //create marker
                haveDestination = true;
                lati = (float)latLng.latitude;
                longi = (float)latLng.longitude;
                desnitation = new LatLng(lati,longi);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                //add marker to the second point
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(markerOptions);

            }
        });

    }


    private LatLng getLocation(){

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            else{
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location != null){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentLocation = new LatLng(latitude, longitude);
                    return  currentLocation;
                }
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return null;
    }




    private void getDeviceLocation(){
        Log.i(TAG, "getDeviceLocation: getting the device current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: SecurityExecption"+ e.getMessage() );
        }
    }

    private  void moveCamera(LatLng latlng, float zoom){
        Log.d(TAG, "moveCamera: moving camera to : lat :"+ latlng.latitude + ", lng : "+ latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initialising the map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location persmission");
        String[] permissions = {FINE_LOCATION,
                COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_GRANTED_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_GRANTED_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: override onRequestPermissionsResult method called");
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_GRANTED_CODE:{
                if(grantResults.length >0){
                    for(int i =0; i< grantResults.length ; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //initialise our map
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    initMap();
                }
            }
        }
    }


    public class JsonParser extends AsyncTask<Void, Void, String>{
        String json_url;
        String json_string;

        @Override
        protected void onPreExecute() {
            json_url = "http://192.168.43.144/mapPHP/client.php";
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                //httpURLConnection.connect();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedwritter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));
                String postdata = URLEncoder.encode("lat", "UTF-8") + "=" + URLEncoder.encode(Float.toString(lati), "UTF-8")
                        + "&" + URLEncoder.encode("lon", "UTF-8") + "=" + URLEncoder.encode(Float.toString(longi), "UTF-8");
                bufferedwritter.write(postdata);
                bufferedwritter.flush();
                bufferedwritter.close();
                outputstream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line+"\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: "+result);
            //getJson(result);
            json_string = result;
            if(result!=null) {
                if (result.equals("Success")) {
                    Toast.makeText(MapActivity.this, "Device Got the response!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MapActivity.this, "Can't connect to the server!", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

}
