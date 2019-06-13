package com.example.anonymous.googlemaps;

import android.app.Dialog;
import android.app.MediaRouteButton;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.nearby.connection.Strategy;

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
import java.util.List;

import static com.example.anonymous.googlemaps.R.id.parent;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_RESULT = 9001;
    private float lat,lon;

    ActionBar actionBar;
    Button btn_map;
    TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3C3F41")));

        if(isServicesOK()){
            btn_map = (Button) findViewById(R.id.btn_map);
            tx = (TextView) findViewById(R.id.textView1);

            btn_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("lat",Float.toString(lat));
                    extras.putString("lon",Float.toString(lon));
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });

            JsonParse jsonParse = new JsonParse();
            jsonParse.execute();
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and user can make request
            Log.d(TAG, "isServicesOK: Google play services are working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK:  an error occurred but we can resolve it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_RESULT);
            dialog.show();
        }
        else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return true;

    }


    public class JsonParse extends AsyncTask<Void, Void, String> {
        String json_url;
        String json_string;
        JSONObject jsonObject;
        JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            json_url = "http://192.168.43.144/mapPHP/client.php";
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
            try {
                if(result != null) {
                    jsonObject = new JSONObject(json_string);
                    jsonArray = jsonObject.getJSONArray("locations");
                    JSONObject JO = jsonArray.getJSONObject(0);
                    lat = (float) JO.getDouble("lat");
                    lon = (float) JO.getDouble("lon");
                    String location = Float.toString(lat) + "  " + Float.toString(lon);
                    tx.setText(location);
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

