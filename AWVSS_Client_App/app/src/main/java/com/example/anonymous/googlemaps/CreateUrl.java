package com.example.anonymous.googlemaps;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ANONYMOUS on 25-Apr-18.
 */

public class CreateUrl {
    public static String getRequestUrl(LatLng origin, LatLng dest, LatLng waypoint) {
        //value of origin
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //value of waypoints
        String str_way = "waypoints=" + waypoint.latitude + "," +waypoint.longitude;
        //mode for find direction
        String mode = "mode-driving";
        //Build the full param
        String param = str_org +"&"+ str_dest+"&"+ str_way +"&"+ mode;
        //output format
        String output = "json";

        //CREATE URL TO REQUEST
        String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + param;
        return url;
    }

    public static String getRequestURL(ArrayList<LatLng> listpoints){
        String param = "";
        for(int i =0 ; i < listpoints.size() ; i++){
            if(i==0) {
                //value of origin
                String str_org = "origin=" + listpoints.get(i).latitude + "," + listpoints.get(i).longitude;
                param += str_org + "&";

            }
            else if(i==1){
                //value of destination
                String str_dest = "destination=" + listpoints.get(i).latitude + "," + listpoints.get(i).longitude;
                param += str_dest+"&";
            }

            else if(i==2){
                //value of waypoints
                String str_way = "waypoints=" + listpoints.get(i).latitude + "," +listpoints.get(i).longitude;
                param += str_way;
            }

            else{
                String str_way = "|"+ listpoints.get(i).latitude + "," +listpoints.get(i).longitude;
                param += str_way;
            }

        }
        //mode for find direction
        String mode = "&mode-driving";
        if(listpoints.size()==2) {
            mode = "mode-driving";
        }
        param += mode;
        //output format
        String output = "json";

        //CREATE URL TO REQUEST
        String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + param;
        return url;
    }
}
