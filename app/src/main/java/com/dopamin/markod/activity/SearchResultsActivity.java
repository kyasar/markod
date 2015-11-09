package com.dopamin.markod.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.R;
import com.dopamin.markod.animation.ExpandListviewAnimation;
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.ScanProductsRequest;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.request.PlacesResult;
import com.dopamin.markod.request.PlacesTask;
import com.dopamin.markod.adapter.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class SearchResultsActivity extends AppCompatActivity
        implements LocationListener, PlacesResult, AdapterView.OnItemClickListener {

    private LocationManager locationManager = null;
    private GoogleMap googleMap = null;
    private double latitude = 0;
    private double longitude = 0;
    private int PROXIMITY_RADIUS = 500;
    private String selectedMarketName = null;
    protected AlertDialog.Builder builder;
    private MarketListAdapter adapter;
    private ProgressDialog progressDialog;
    private User user;
    private List<Product> productSearchList;
    private ListView lv_markets;
    private FrameLayout view_map_fragment;
    boolean fakeLocation = true;

    private Toolbar toolbar;

    private List<Market> nearbyMarkets = null;
    HashMap <String, String> mMarkerPlaceLink = new HashMap <String, String> ();

    String scanURL = MainActivity.MDS_SERVER + "/mds/api/scan/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show error dialog if GooglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_search_results);

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lv_markets = (ListView) findViewById(R.id.list);

        view_map_fragment = (FrameLayout) findViewById(R.id.id_fl_map);
        view_map_fragment.setVisibility(View.GONE);

        /* Nearby Markets loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.str_searching_products);
        progressDialog.setMessage(getResources().getString(R.string.str_searching_products_nearby_markets));
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
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.results_map_fragment)).getMap();
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

                for (int i = 0; i < nearbyMarkets.size(); i++) {
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
         * On selecting a listitem Animation expand/collapse plays and product list details are viewed
         * */
        lv_markets.setOnItemClickListener(this);

        Bundle bundle = getIntent().getExtras();
        productSearchList = getIntent().getParcelableArrayListExtra("searchProductList");
        user = bundle.getParcelable("user");

        /* Search nearby markets and list them in Listview */
        if (productSearchList != null) {
            Log.v(MainActivity.TAG, "# of Products: " + productSearchList.size());
            for (Product p : productSearchList) {
                Log.v(MainActivity.TAG, "Product: " + p.getName() + " barcode: " + p.getBarcode());
            }
        } else {
            Log.e(MainActivity.TAG, "No product Search List !!");
        }

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

        Log.v(MainActivity.TAG, "GMAP query Sentence: " + sb.toString());

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
        if (id == android.R.id.home) {
            finish();
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

    private List<Product> jsonArrayToProductList(JSONArray jsonProducts) {
        List<Product> products = new ArrayList<Product>();
        JSONObject jsonP = null;
        Product p = null;

        for (int i=0; i < jsonProducts.length(); i++) {
            try {
                jsonP = jsonProducts.getJSONObject(i);
                p = new Product(null, jsonP.getString("barcode"), jsonP.getString("price"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            products.add(p);
        }

        return products;
    }

    private void detectMissingProducts(Market market) {
        // productSearchList contains user's list to search products
        int i;
        for (Product p : productSearchList) {
            for (i=0; i < market.getProducts().size(); i++) {
                if (market.getProducts().get(i).getBarcode().equalsIgnoreCase(p.getBarcode()))
                    break;
            }
            if (i == market.getProducts().size()) {
                market.incMissing();
            }
        }
    }

    private void parseScanMarketsRespond(JSONArray scannedJSONMarkets) {
        List<Market> filteredMarkets = new ArrayList<Market>();
        Market market = null;

        for (int i=0; i < scannedJSONMarkets.length(); i++) {
            JSONObject m = null;
            try {
                m = scannedJSONMarkets.getJSONObject(i);
                for(int j=0; j < nearbyMarkets.size(); j++) {
                    if (nearbyMarkets.get(j).getId().equalsIgnoreCase(m.getString("id"))) {
                        market = nearbyMarkets.get(j);
                        market.setProducts(jsonArrayToProductList(m.getJSONArray("products")));
                        market.calculateProductList();
                        detectMissingProducts(market);
                        break;
                    }
                }

                if (market != null) {
                    filteredMarkets.add(market);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Set global markets - filtered
        nearbyMarkets = filteredMarkets;

        Collections.sort(nearbyMarkets, new Comparator() {

            public int compare(Object o1, Object o2) {

                Integer x1 = ((Market) o1).getMissing();
                Integer x2 = ((Market) o2).getMissing();
                int comp = x1.compareTo(x2);

                if (comp != 0) {
                    return comp;
                } else {
                    x1 = ((Market) o1).getpMain();
                    x2 = ((Market) o2).getpMain();
                    comp = x1.compareTo(x2);

                    if (comp != 0) {
                        return comp;
                    } else {
                        x1 = ((Market) o1).getpCent();
                        x2 = ((Market) o2).getpCent();
                        return x1.compareTo(x2);
                    }
                }
            }
        });
    }

    private void printMarkets() {
        //DEBUG
        for (Market m : nearbyMarkets) {
            Log.v(MainActivity.TAG, "Market: " + m.getName()
                    + " (" + m.getId() + ") has products.. Missing: " + m.getMissing()
                    + " Total Main: " + m.getpMain() + " Cent: " + m.getpCent());
            for (int i=0; i < m.getProducts().size(); i++) {
                Log.v(MainActivity.TAG, "   #" + i + " Product price: "
                        + m.getProducts().get(i).getpMain() + "." + m.getProducts().get(i).getpCent());
            }
        }
    }

    @Override
    public void processPlaces(List<Market> markets) {
        // set global places list before use it
        nearbyMarkets = markets;

        //progressDialog.dismiss();
        progressDialog.setMessage(getResources().getString(R.string.str_getting_best_prices));

        for (Market m : nearbyMarkets) {
            Marker marker = googleMap.addMarker(m.getMarkerOptions());
            mMarkerPlaceLink.put(marker.getId(), m.getReference());
        }

        ScanProductsRequest scanRequest = new ScanProductsRequest();
        scanRequest.setMarkets(nearbyMarkets);
        scanRequest.setProducts(productSearchList);

        /* TODO: this part exclude some extra fields from JSON
            We make these fields protected and static, but maybe in future it can be a problem
            Be Careful !!
         */
        Gson gson = new Gson();
        Log.v(MainActivity.TAG, "Market JSON: " + gson.toJson(scanRequest));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, scanURL
                + "?api_key=" + MainActivity.MDS_API_KEY,
                gson.toJson(scanRequest), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("volley", "response: " + response);

                try {
                    if (response.get("status").toString().equalsIgnoreCase("OK")) {
                        JSONArray jsonMarkets = (JSONArray) response.get("markets");
                        parseScanMarketsRespond(jsonMarkets);
                        printMarkets();

                        adapter = new MarketListAdapter(getApplicationContext(), nearbyMarkets, MarketListAdapter.LIST_TYPE.MARKET_SCAN);
                        lv_markets.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("volley", "error: " + error);
                progressDialog.dismiss();
            }
        });
        Volley.newRequestQueue(getApplication()).add(jsObjRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // getting values from selected ListItem
        final Market market = (Market) adapterView.getAdapter().getItem(i);
        Product productServer;
        LinearLayout llPview;
        TextView tvP, tvN;
        int j;

        Log.v(MainActivity.TAG, "List-view item is clicked (" + market.getName() + "). OK.");

        LinearLayout lay_results_bar = (LinearLayout) view.findViewById(R.id.id_results_bar);
        LinearLayout lay_products_details = (LinearLayout) lay_results_bar.findViewById(R.id.id_products_details);
        lay_products_details.removeAllViews();

        /* Search product in Product list and use info in local product list
         * Because list from server just contains price and barcode, No name !! */
        for (Product productLocal :  productSearchList)
        {
            llPview = (LinearLayout) getLayoutInflater().inflate(R.layout.product_price_item, null);
            tvN = (TextView) llPview.findViewById(R.id.product_name);
            tvP = (TextView) llPview.findViewById(R.id.product_price);

            for (j = 0; j < market.getProducts().size(); j++)
            {
                productServer = market.getProducts().get(j);

                if (productLocal.getBarcode().equalsIgnoreCase(productServer.getBarcode())) {

                    // Log.v(MainActivity.TAG, "Product Matches: " + productLocal.getBarcode() + " - " + productServer.getBarcode());
                    // Name comes from local
                    tvN.setText(productLocal.getName());

                    // Price comes from server
                    tvP.setText(productServer.getPrice());

                    break;
                }
            }

            /* Product in search list not found in this market */
            if (j >= market.getProducts().size()) {
                // Log.v(MainActivity.TAG, "Product NOT Matches: " + productLocal.getBarcode());
                // Name comes from local, overline and gray style for not found products
                tvN.setText(productLocal.getName());
                tvN.setPaintFlags(tvN.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvN.setTextColor(R.color.gray);
            }

            lay_products_details.addView(llPview);
        }

        // Creating the expand animation for the item
        ExpandListviewAnimation expandAni = new ExpandListviewAnimation(lay_results_bar, 500);

        LinearLayout lay_action_tools = (LinearLayout) lay_results_bar.findViewById(R.id.id_action_tools);
        ImageButton order_btn = (ImageButton) lay_action_tools.findViewById(R.id.id_order_to_market);
        order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(MainActivity.TAG, market.getName() + " does not deliver orders !!");
            }
        });

        ImageButton maps_btn = (ImageButton) lay_action_tools.findViewById(R.id.id_open_in_maps);
        maps_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(MainActivity.TAG, market.getName() + " will be shown on Maps..");
            }
        });

        // Start the animation on the toolbar
        lay_results_bar.startAnimation(expandAni);
    }
}
