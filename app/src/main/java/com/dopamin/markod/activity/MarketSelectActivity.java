package com.dopamin.markod.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.adapter.*;
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.request.PlacesResult;
import com.dopamin.markod.request.PlacesTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


public class MarketSelectActivity extends FragmentActivity implements LocationListener, PlacesResult {

    private LocationManager locationManager = null;
    private GoogleMap googleMap = null;
    private double latitude = 0;
    private double longitude = 0;
    private int PROXIMITY_RADIUS = 500;
    private String selectedMarketName = null;
    protected AlertDialog.Builder builder;
    private MarketListAdapter adapter;
    private ProgressDialog progressDialog;
    private ListView lv_markets;
    private EditText tv_marketFilter;
    boolean fakeLocation = false;

    private List<Market> nearbyMarkets = null;
    HashMap <String, String> mMarkerPlaceLink = new HashMap <String, String> ();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show error dialog if GooglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        setContentView(R.layout.activity_market_select);
        lv_markets = (ListView) findViewById(R.id.list);
        tv_marketFilter = (EditText) findViewById(R.id.id_tv_marketFilter);

        tv_marketFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        /* Nearby Markets loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.markets_progress);
        progressDialog.setMessage(getResources().getString(R.string.markets_progress_message));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        /* Google Maps API Location Manager */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Log.v(MainActivity.TAG, "Location Provider is " + bestProvider + " OK.");
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 2000, 100, this);

        if (googleMap == null)
        {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment)).getMap();
            googleMap.setMyLocationEnabled(true);
            Log.v(MainActivity.TAG, "Google Map fragment is OK.");
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker arg0) {
                Log.v(MainActivity.TAG, "onInfoWindowClick Listener is invoked.");
                Intent intent = new Intent(getBaseContext(), PlaceDetailsActivity.class);
                String reference = mMarkerPlaceLink.get(arg0.getId());
                intent.putExtra("reference", reference);

                // Starting the Place Details Activity
                startActivity(intent);
            }
        });

		/* Alert Dialog setting for Selecting the Market from listview */
        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                Log.v(MainActivity.TAG, "Market Selected.");

                Market market = null;

                for (int i=0; i < nearbyMarkets.size(); i++) {
                    // Getting a place from the places list
                    market = nearbyMarkets.get(i);

                    if (market.getName().equals(selectedMarketName)) {
                        break;
                    }
                }

                if (market != null) {
                    Intent output = new Intent();
                    saveMarket(market);
                    setResult(RESULT_OK, output);
                    finish();
                } else {
                    Log.e(MainActivity.TAG, "Fatal Problem !! Selected Market not found in placeList.");
                }
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                Log.v(MainActivity.TAG, "Market NOT Selected.");
                dialog.dismiss();
            }
        });

        /**
         * ListItem click event
         * On selecting a listitem SinglePlaceActivity is launched
         * */
        lv_markets.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                selectedMarketName = ((TextView) view.findViewById(R.id.place_name)).getText().toString();
                Log.v(MainActivity.TAG, "Listview item is clicked (" + selectedMarketName + "). OK.");

                builder.setMessage(R.string.select_this_market);
                builder.setTitle(selectedMarketName);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        if (savedInstanceState != null) {
            nearbyMarkets = (List<Market>) savedInstanceState.getSerializable("nearbyMarkets");
            Log.v(MainActivity.TAG, "Restoring market LIST.. size: " + nearbyMarkets.size());

            // list adapter
            adapter = new MarketListAdapter(this, nearbyMarkets);
            lv_markets.setAdapter(adapter);
        }

        /* Search nearby markets and list them in Listview */
        locateMe();
    }

    private void locateMe()
    {
        String type = "grocery_or_supermarket";
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + latitude + "," + longitude);
        sb.append("&radius=" + PROXIMITY_RADIUS);
        sb.append("&types=" + type);
        sb.append("&sensor=true");
        sb.append("&key=" + MainActivity.GOOGLE_API_KEY);

        Log.v(MainActivity.TAG, "GMAP query Rentence: " + sb.toString());

        googleMap.clear();

        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();

        // Result will be returned to this Activity
        placesTask.delegate = this;

        // Invokes the "doInBackground()" method of the class PlaceTask
        progressDialog.show();
        placesTask.execute(sb.toString());
        Log.v(MainActivity.TAG, "Places request sent.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            Log.v(MainActivity.TAG, "GooglePlayServices is available. OK.");
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            if (fakeLocation) {
                latitude = 39.893621;	// My home, sweet home..
                longitude = 32.801732;
            } else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

			/* TODO:
			 *  Focus on when user needs it
			 * change this section when the user needs to pick a place
			 */
            if (googleMap != null) {
                // TODO: Do not focus on every gps data ready
                LatLng myCoordinates = new LatLng(latitude, longitude);
                Log.i(MainActivity.TAG, "Re-focusing on Google Map..");
                googleMap.addMarker(new MarkerOptions().position(myCoordinates).title("You are here"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, 15));
            }

            //Log.i("Geo_Location", "Latitude: " + latitude + ", Longitude: " + longitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        Log.v(MainActivity.TAG, "onSaveInstanceState Spy Market Activity.");
        savedState.putSerializable("nearbyMarkets", (Serializable) nearbyMarkets);
    }

    public boolean saveMarket(Market market) {
        Gson gson = new Gson();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("market", gson.toJson(market));
        Log.v(MainActivity.TAG, "Market saved into Shared.");
        return edit.commit();
    }

    @Override
    public void processPlaces(List<Market> markets) {
        // set global places list before use it
        nearbyMarkets = markets;
        adapter = new MarketListAdapter(getApplicationContext(), nearbyMarkets);
        lv_markets.setAdapter(adapter);
        progressDialog.dismiss();

        for (Market m : nearbyMarkets) {
            Marker marker = googleMap.addMarker(m.getMarkerOptions());
            mMarkerPlaceLink.put(marker.getId(), m.getReference());
        }
    }
}
