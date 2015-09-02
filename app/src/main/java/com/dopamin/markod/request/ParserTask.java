package com.dopamin.markod.request;

import android.os.AsyncTask;
import android.util.Log;

import com.dopamin.markod.PlaceJSONParser;
import com.dopamin.markod.objects.Market;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

        // Clears all the existing markers
        //googleMap.clear();
        List<Market> nearbyMarkets = new ArrayList<Market>();
        List<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();

        for (int i = 0; i < list.size(); i++) {

            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions();

            // Getting a place from the places list
            HashMap<String, String> hmPlace = list.get(i);

            // Getting latitude of the place
            double lat = Double.parseDouble(hmPlace.get("lat"));

            // Getting longitude of the place
            double lng = Double.parseDouble(hmPlace.get("lng"));

            // Getting name
            String name = hmPlace.get("place_name");

            // Getting vicinity
            String vicinity = hmPlace.get("vicinity");

            Market m = new Market(name, hmPlace.get("place_id"), vicinity);
            nearbyMarkets.add(m);

            LatLng latLng = new LatLng(lat, lng);

            // Setting the position for the marker
            markerOptions.position(latLng);

            // Setting the title for the marker.
            //This will be displayed on taping the marker
            markerOptions.title(name + " : " + vicinity);

            markerOptionsList.add(markerOptions);

            // Placing a marker on the touched position
            //Marker marker = googleMap.addMarker(markerOptions);

            // Linking Marker id and place reference
            //mMarkerPlaceLink.put(marker.getId(), hmPlace.get("reference"));
        }

        // list adapter
        //adapter = new MarketListAdapter(getApplicationContext(), nearbyMarkets);

        // This is needed to fill listview with first results
        //lv_markets.setAdapter(adapter);

        // Dismiss the progress dialog
        //progressDialog.dismiss();
        delegate.processPlaces(nearbyMarkets);
        delegate.processMarkers(markerOptionsList);
    }
}
