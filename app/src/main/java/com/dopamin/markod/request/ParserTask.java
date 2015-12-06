package com.dopamin.markod.request;

import android.os.AsyncTask;
import android.util.Log;

import com.dopamin.markod.PlaceJSONParser;
import com.dopamin.markod.objects.Market;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** A class to parse the Google Places in JSON format */
public class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {

    public PlacesResult delegate = null;
    JSONObject jObject;

    // Invoked by execute() method of this object
    @Override
    protected List<HashMap<String,String>> doInBackground(String... jsonData) {

        List<HashMap<String, String>> places = null;
        PlaceJSONParser placeJsonParser = new PlaceJSONParser();

        try {
            jObject = new JSONObject(jsonData[0]);

            /** Getting the parsed data as a List construct */
            places = placeJsonParser.parse(jObject);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return places;
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(List<HashMap<String,String>> list) {

        List<Market> nearbyMarkets = new ArrayList<Market>();
        String name, vicinity, reference;

        //TODO: List may return empty ! NullPointer exception
        for (int i = 0; i < list.size(); i++) {

            // Getting a place from the places list
            HashMap<String, String> hmPlace = list.get(i);

            // Getting latitude of the place
            double lat = Double.parseDouble(hmPlace.get("lat"));

            // Getting longitude of the place
            double lng = Double.parseDouble(hmPlace.get("lng"));

            // Getting name
            name = hmPlace.get("place_name");

            // Getting vicinity
            vicinity = hmPlace.get("vicinity");

            // Getting reference to link
            reference = hmPlace.get("reference");

            Market m = new Market(name, hmPlace.get("place_id"), vicinity, reference);

            LatLng latLng = new LatLng(lat, lng);

            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions();

            // Setting the position for the marker
            markerOptions.position(latLng);

            // markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_market));

            // Setting the title for the marker.
            //This will be displayed on taping the marker
            markerOptions.title(name + " : " + vicinity);
            m.setMarkerOptions(markerOptions);
            m.setLoc(markerOptions.getPosition().latitude + "," + markerOptions.getPosition().longitude);

            nearbyMarkets.add(m);
        }

        // Dismiss the progress dialog
        delegate.processPlaces(nearbyMarkets);
    }
}
