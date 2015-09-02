package com.dopamin.markod.request;

import android.os.AsyncTask;
import android.util.Log;

import com.dopamin.markod.PlaceJSONParser;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.objects.Market;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kadir on 02.09.2015.
 */
/** A class, to download Google Places */
public class PlacesTask extends AsyncTask<String, Integer, String> {

    public PlacesResult delegate = null;
    String data = null;

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();

            //Log.v(TAG, "RES: " + data);
            br.close();

        } catch (Exception e) {
            Log.v(MainActivity.TAG, "Exception while downloading url");
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }


    // Invoked by execute() method of this object
    // Does not affect main activity
    @Override
    protected String doInBackground(String... url) {
        try {
            data = downloadUrl(url[0]);
        } catch (Exception e){
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    // Executed after the complete execution of doInBackground() method
    // This method is executed in Main Activity
    @Override
    protected void onPostExecute(String result) {
        ParserTask parserTask = new ParserTask();
        parserTask.delegate = delegate;

        // Start parsing the Google places in JSON format
        // Invokes the "doInBackground()" method of the class ParseTask
        parserTask.execute(result);
    }
}

