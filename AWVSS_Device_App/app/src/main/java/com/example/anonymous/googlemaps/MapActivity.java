package com.example.anonymous.googlemaps;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.Set;
import java.util.UUID;

import static java.lang.Math.abs;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,SensorEventListener {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_GRANTED_CODE = 1234;
    private static final String DEVICE_ADDRESS = "00:18:E4:40:00:06"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private boolean haveBluetoothConnected;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String forward = "F";
    private static final String backward = "B";
    private static final String left = "L";
    private static final String right = "R";
    private static final String stop = "S";
    private static final String forwardLeft = "L";
    private static final String forwardRight = "R";

    private Handler mainThread = new Handler();
    //variables
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<LatLng> listpoints;
    private LatLng currentLatLng=null;
    private LatLng currentLatLng1=null;
    public String JSON_STRING = "";
    public String flag = "";
    ActionBar actionBar;
    PolylineOptions polylineOptions = null;


    //Compass Sensors
    TextView textViewDegree;
    TextView textDestination;
    TextView textDistance;
    TextView textDirection;
    Button btn_start;
    Button btn_connect;
    //luetoothSPP bluetooth;

    private volatile int mAzimuth;
    private volatile boolean startRouting;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    float[] rMat = new float[9];
    float[] orientation = new float[9];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];

    private boolean haveSensor = false, haveSensor2 = false;
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    float degree_destination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3C3F41")));

        getLocationPermission();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        listpoints = new ArrayList<>();
        startRouting = false;
        haveBluetoothConnected = false;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //bluetooth = new BluetoothSPP(this);
        textViewDegree = (TextView)findViewById(R.id.textView_Degree);
        textDestination = (TextView) findViewById(R.id.textDestination);
        textDistance = (TextView) findViewById(R.id.textDistance);
        textDirection = (TextView) findViewById(R.id.text_direction);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_connect = (Button) findViewById(R.id.btn_connect);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(BTinit())
            {
                BTconnect();
            }
            }
        });

        /*btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setText("Started");
                btn_start.setBackgroundColor(Color.GREEN);
                if(haveBluetoothConnected) {
                    if (startRouting)
                        startRouting = false;
                    else {
                        startRouting = true;
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Bluetooth device not connected", Toast.LENGTH_LONG).show();
                }
            }
        });
        */

        //CheckUserGivesLocation checkUserGivesLocation = new CheckUserGivesLocation(100);
        //new Thread(checkUserGivesLocation).start();

        start();
        flag = getIntent().getStringExtra("flag");
    }

    //Initializes bluetooth module
    public boolean BTinit()
    {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }

        if(!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);

            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public boolean BTconnect()
    {
        boolean connected = true;

        try
        {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();

            Toast.makeText(getApplicationContext(),
                    "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();
            btn_connect.setBackgroundColor(Color.GREEN);
            haveBluetoothConnected = true;

        }
        catch(IOException e)
        {
            e.printStackTrace();
            connected = false;
        }

        if(connected)
        {
            try
            {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return connected;
    }

    private void toastText(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    public void onStart() {
        super.onStart();
    }


    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        currentLatLng = getLocation();                          //Get My Location
        Log.d(TAG, "OnMapReady : Current Location : "+currentLatLng);
        if(currentLatLng != null){
            CurrentLocationUpdate currentLocationUpdate = new CurrentLocationUpdate();
            currentLocationUpdate.execute(currentLatLng);
        }


        Log.d(TAG, "onMapReady: Json Output : "+JSON_STRING);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //reset marker when already 2


            }
        });

    }



    private void start(){
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)==null){
            if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)==null || mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)==null){
                noSensorAlert();
            }
            else{
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the compass.")
                    .setCancelable(false)
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
    }

    public void stop(){
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }
        else{
            if(haveSensor){
                mSensorManager.unregisterListener(this, mMagnetometer);
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        stop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        start();
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


    // TODO: 4/7/2019  Compass Sensor Calculation

    @Override
    public void onSensorChanged(SensorEvent event) {
        CompassSensorUpdateRunnable compassSensorUpdateRunnable = new CompassSensorUpdateRunnable(event);
        new Thread(compassSensorUpdateRunnable).start();
        if(haveBluetoothConnected) {
            if (startRouting) {
                DirectionOfDestinationRunnable directionOfDestinationRunnable = new DirectionOfDestinationRunnable();
                new Thread(directionOfDestinationRunnable).start();
            } else {
                JsonParseDestination jsonParseDestination = new JsonParseDestination();
                jsonParseDestination.execute();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class CompassSensorUpdateRunnable implements Runnable
    {
        SensorEvent event;

        CompassSensorUpdateRunnable(SensorEvent event){
            this.event = event;
        }
        @Override
        public void run() {
            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                SensorManager.getRotationMatrixFromVector(rMat, event.values);
                mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0])+360)%360);
            }
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                System.arraycopy(event.values,0,mLastAccelerometer,0,event.values.length);
                mLastAccelerometerSet = true;
            }
            else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                System.arraycopy(event.values,0,mLastMagnetometer,0,event.values.length);
                mLastMagnetometerSet= true;
            }

            if(mLastAccelerometerSet && mLastMagnetometerSet){
                SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(rMat, orientation);
                mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0])+360)%360);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onSensorChanged: "+ mAzimuth);
                    mAzimuth = Math.round(mAzimuth);
                    textViewDegree.setText(Integer.toString(mAzimuth));
                }
            });

        }
    }

    public class DirectionOfDestinationRunnable implements Runnable{
        String command;
        double distance = distanceOf2Points(getLocation(), listpoints.get(1));
        @Override
        public void run() {
            if(distance<0.00007){
                command = stop;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                startRouting = false;
            }
            if (abs(mAzimuth - degree_destination) < 7) {
                command = forward;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if (mAzimuth > degree_destination && mAzimuth < degree_destination + 180) {
                command = forwardLeft;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if (mAzimuth > degree_destination && mAzimuth > degree_destination + 180) {
                command = forwardRight;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if (mAzimuth < degree_destination && mAzimuth > degree_destination - 180) {
                command = forwardRight;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if (mAzimuth < degree_destination && mAzimuth < degree_destination - 180) {
                command = forwardLeft;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else{
                command = stop;
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "onClick: the direction for going : " + command);
            Log.d(TAG, "onClick: the direction for going : " + mAzimuth);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onSensorChanged: "+ mAzimuth);
                    textDirection.setText(command);
                    float d = (float) distance;
                    textDistance.setText(Float.toString(d));
                    //textViewDegree.setText(Integer.toString(mAzimuth));
                }
            });
        }
    }


    public double distanceOf2Points(LatLng p1, LatLng p2){
        double distance = Math.sqrt((p2.latitude-p1.latitude)*(p2.latitude-p1.latitude)
                +(p2.longitude-p1.longitude)*(p2.longitude-p1.longitude));
        return distance;
    }

    /*
    public class CheckUserGivesLocation implements Runnable{
        int seconds;

        CheckUserGivesLocation(int sec){
            this.seconds = sec;
        }
        @Override
        public void run() {
            for(int i=0; i<seconds; i++){
                try {
                    if(startRouting){
                        break;
                    }
                    Log.d(TAG, "CheckUserGivesLocation run: "+i);
                    JsonParseDestination jsonParseDestination = new JsonParseDestination();
                    jsonParseDestination.execute();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    */
    public class JsonParseDestination extends AsyncTask<Void, Void, String> {
        String json_url;
        String json_string;
        JSONObject jsonObject;
        JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            json_url = "http://192.168.43.144/mapPHP/destination.php";
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
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
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: " + result);
            json_string = result;
            //List<Float> check = new ArrayList<>();
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            if(result != null) {
                try {
                    currentLatLng1 = getLocation();
                    if(currentLatLng1 != null){
                        listpoints.add(currentLatLng1);
                    }
                    jsonObject = new JSONObject(json_string);
                    jsonArray = jsonObject.getJSONArray("locations");

                    JSONObject JO = jsonArray.getJSONObject(0);
                    float lat = (float) JO.getDouble("lat");
                    float lon = (float) JO.getDouble("lon");

                    LatLng l = new LatLng(lat, lon);
                    //save first point select
                    listpoints.add(l);
                    //create marker
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(l);

                    //add marker to the second point
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mMap.addMarker(markerOptions);

                    polylineOptions = new PolylineOptions();
                    polylineOptions.addAll(listpoints);
                    polylineOptions.width(10);
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.geodesic(true);

                    if(polylineOptions != null){
                        mMap.addPolyline(polylineOptions);
                    }

                    double x0 = listpoints.get(0).latitude;
                    double y0 = listpoints.get(0).longitude;
                    double x1 = listpoints.get(1).latitude;
                    double y1 = listpoints.get(1).longitude;

                    degree_destination = (float) Math.toDegrees(Math.atan2(y1-y0,x1-x0));
                    if(degree_destination < 0 ){
                        degree_destination = degree_destination + 360;
                    }

                    float distance = (float) Math.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0));

                    Log.d(TAG, "onMapLongClick: the degree of the line is "+degree_destination);
                    textDestination.setText(Float.toString(degree_destination));
                    textDistance.setText(Float.toString(distance));

                    if(haveBluetoothConnected) {
                        startRouting = true;
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Bluetooth device not connected", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    public class CurrentLocationUpdate extends AsyncTask<LatLng, Void, String> {
            String json_url;

            @Override
            protected void onPreExecute() {
                json_url = "http://192.168.43.144/mapPHP/destination.php";

            }

            @Override
            protected String doInBackground(LatLng... params) {
                try {
                    LatLng location = params[0];

                    URL url = new URL(json_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputstream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedwritter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));
                    String postdata = URLEncoder.encode("lat", "UTF-8") + "=" + URLEncoder.encode(Double.toString(location.latitude), "UTF-8")
                            + "&" + URLEncoder.encode("lon", "UTF-8") + "=" + URLEncoder.encode(Double.toString(location.longitude), "UTF-8");
                    bufferedwritter.write(postdata);
                    bufferedwritter.flush();
                    bufferedwritter.close();
                    outputstream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
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
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, "onPostExecute: " + result);

                try {
                    if(result != null){
                        if(result.equals("Success")){
                            Toast.makeText(getApplicationContext(), "Current Location Updated", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Cant Upload Current Location", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }


}
