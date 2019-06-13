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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.anonymous.googlemaps.R.id.parent;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_RESULT = 9001;
    static MediaPlayer mp;

    ActionBar actionBar;
    Spinner spinner;
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
            spinner = (Spinner) findViewById(R.id.spinner);
            tx = (TextView) findViewById(R.id.textView1);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.droplist, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(this);

            spinner.setVisibility(View.INVISIBLE);//invisible the spinner

            btn_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("flag", "false");
                    startActivity(intent);
                }
            });

            //JsonParse jsonParse = new JsonParse();
            //jsonParse.execute();
        }
    }


    protected void playSound(){
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.quite_impressed);
        mp.start();
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //String selected = parent.getItemAtPosition(position).toString();
        //Toast.makeText( this, selected, Toast.LENGTH_SHORT).show();
        if(position == 1 ){
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("flag", "true");
            startActivity(intent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public class JsonParse extends AsyncTask<Void, Void, String> {
        String json_url;
        String json_string;
        JSONObject jsonObject;
        JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            json_url = "http://waste.dgted.com/initandroid.php";
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
            List<Float> check = new ArrayList<>();
            try {
                jsonObject = new JSONObject(json_string);
                jsonArray = jsonObject.getJSONArray("locations");

                int loopcount = 0;
                int flag = 0;

                while (loopcount < jsonArray.length()) {
                    JSONObject JO = jsonArray.getJSONObject(loopcount);
                    int ratio = JO.getInt("ratio");
                    if(ratio <= 4){
                        flag++;
                    }
                    loopcount++;
                }
                if(flag>0){
                    spinner.setVisibility(View.VISIBLE);
                    String txtView = "The Number of Full Smartbin is "+ String.valueOf(flag);
                    tx.setText(txtView);
                    playSound();

                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

