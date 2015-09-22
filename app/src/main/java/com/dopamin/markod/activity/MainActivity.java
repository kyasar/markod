package com.dopamin.markod.activity;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.dopamin.markod.R;
import com.dopamin.markod.adapter.*;
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.User;
import com.google.gson.Gson;
import com.dopamin.markod.search.SearchBox;
import com.dopamin.markod.search.SearchResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener,
        View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String MDS_TOKEN = "test";
    public static boolean internetConn = false;
    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    public static String MDS_SERVER = "http://192.168.43.120:8000";

    private Button btn_delete_searchTxt, btn_spy_market, btn_checkIntConn, btn_backMain,
                    btn_profile, btn_campaign, btn_declare_product;

    /* Select market request for the Market Select Activity */
    private int SELECT_MARKET_REQUESTCODE = 1;

    /* User request for Login Activity */
    private int LOGIN_FOR_SPY_REQUESTCODE = 2;
    private int LOGIN_FOR_ADD_PRODUCT_REQUESTCODE = 3;
    private int LOGIN_FOR_PROFILE_REQUESTCODE = 4;

    public static final int BARCODE_REQUEST = 1071;
    public static final int CAMERA_REQUEST = 1453;

    public static String TAG = "MDlog";

    /* Objects */
    public static User user;
    public static Market market;

    private TextView loginNameTxt;
    private TextView marketNameTxt;
    private AutoCompleteTextView ac_tv_product_search;

    private SliderLayout mDemoSlider;
    private Menu menu;
    private MenuItem searchMenuItem;

    private boolean mSearchOpened = false;

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

    private SearchBox searchBox;
    private List<Product> products = new ArrayList<Product>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        internetConn = isInternetAvailable();

        if (!isInternetAvailable()) //returns true if internet available
        {
            Toast.makeText(this, " No Internet Connection !! \n Check your Connection..", Toast.LENGTH_SHORT).show();
            //TODO: Draw a new layout informing user that there is no connection.
            setContentView(R.layout.activity_main_noconn);
            btn_checkIntConn = (Button) findViewById(R.id.checkConn_button);
            btn_checkIntConn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    internetConn = isInternetAvailable();
                }
            });
            return;
        }
        setContentView(R.layout.activity_main);

        mNavItems.add(new NavItem("Home", "Your Profile", R.drawable.ico_market));
        mNavItems.add(new NavItem("Shop Lists", "ShopLists and Favourites", R.drawable.ico_market));
        mNavItems.add(new NavItem("Preferences", "Change your preferences and Settings", R.drawable.ic_action_settings));
        mNavItems.add(new NavItem("Help", "Help and Usage", R.drawable.ic_action_about));
        mNavItems.add(new NavItem("About", "Get to know about us", R.drawable.ic_action_about));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigation Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ico_market,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "onDrawerOpened: " + getTitle());
                getSupportActionBar().hide();
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: " + getTitle());
                getSupportActionBar().show();
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_main, null);
        if (actionBar != null) {
            actionBar.setCustomView(v);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        getSupportActionBar().setIcon(R.drawable.ico_market);

        // Main Menu buttons: Spy, Declare, Campaign and Profile
        btn_spy_market = (Button) findViewById(R.id.id_btn_spy_market);
        btn_spy_market.setOnClickListener(this);

        btn_profile = (Button) findViewById(R.id.id_btn_profile);
        btn_profile.setOnClickListener(this);

        btn_campaign = (Button) findViewById(R.id.id_btn_campaign);
        btn_campaign.setOnClickListener(this);

        btn_declare_product = (Button) findViewById(R.id.id_btn_declare_product);
        btn_declare_product.setOnClickListener(this);

        // Layouts to change searchBox state or main state
        loginNameTxt = (TextView) findViewById(R.id.login_name_text);
        marketNameTxt = (TextView) findViewById(R.id.market_name_text);

        /* Load Location-based Ads */
        loadAdImages();

        //////////////////////////////


        // SEARCH BOX
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        searchBox.enableVoiceRecognition(this);
        /* for (int x = 0; x < 10; x++){
            SearchResult option = new SearchResult("Result " + Integer.toString(x),
            getResources().getDrawable(R.drawable.ico_points));
            searchBox.addSearchable(option);
        } */
        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                Toast.makeText(MainActivity.this, "Menu click", Toast.LENGTH_LONG).show();
            }
        });

        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                Log.v(MainActivity.TAG, "Search Opened.");
                //searchBox.clearResults();
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                Log.v(MainActivity.TAG, "Search Closed.");
                searchBox.clearResults();
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the searchBox term changing
                //Called after it has updated results
                Log.v(MainActivity.TAG, "TERM: " + searchBox.getSearchText());
                searchBox.showLoading(true);
                getProducts(searchBox.getSearchText());
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(MainActivity.this, searchTerm + " Searched", Toast.LENGTH_LONG).show();
                Log.v(MainActivity.TAG, "Search DONE.");
            }

            @Override
            public void onSearchCleared() {
                Log.v(MainActivity.TAG, "Search Cleared.");
                //Called when the clear button is clicked
            }
        });
    }

    public void getProducts(final String search) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                MainActivity.MDS_SERVER + "/mds/api/products" + "?search=" + search,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MainActivity.TAG, response.toString());

                        try {
                            // TODO: Json respond check status?
                            // Parsing json array response
                            // loop through each json object
                            JSONArray jsonProducts = response.getJSONArray("product");

                            Log.v(MainActivity.TAG, "Product searchBox array respond length: " + jsonProducts.length());
                            searchBox.clearResults();
                            for (int i = 0; i < jsonProducts.length(); i++) {
                                JSONObject p = (JSONObject) jsonProducts.get(i);
                                String name = p.getString("name");
                                String barcode = p.getString("barcode");
                                Product product = new Product(name, barcode);
                                products.add(product);

                                SearchResult option = new SearchResult(product.getName(),
                                        getResources().getDrawable(R.drawable.ico_points));
                                searchBox.addSearchable(option);
                                searchBox.updateResults();
                            }
                            searchBox.showLoading(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(req);
    }


    /*
* Called when a particular item from the navigation drawer
* is selected.
* */
    private void selectItemFromDrawer(int position) {
        Fragment fragment = new PreferencesFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commit();

        mDrawerList.setItemChecked(position, true);
        if (mNavItems.get(position).getTitle().equalsIgnoreCase("Home")) {
            goToProfilePage();
        } else if (mNavItems.get(position).getTitle().equalsIgnoreCase("Shop Lists")) {

        } else if (mNavItems.get(position).getTitle().equalsIgnoreCase("Preferences")) {

        } else if (mNavItems.get(position).getTitle().equalsIgnoreCase("Help")) {

        } else if (mNavItems.get(position).getTitle().equalsIgnoreCase("About")) {

        }

        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == SELECT_MARKET_REQUESTCODE && resultCode == RESULT_OK && data != null) {
            Log.v(TAG, "MainActivity: Market is ready");
            loadMarket();
            loadUser();
            marketNameTxt.setText(market.getName() + " \nid: " + market.getId() + " \n"
                    + market.getVicinity());

            Intent intent = new Intent(getBaseContext(), SpyMarketActivity.class);
            intent.putExtra("market", market);
            intent.putExtra("user", user);
            startActivity(intent);
            Log.v(TAG, "MainActivity: SpyMarketActivity is started. OK.");

        } else if (requestCode == LOGIN_FOR_SPY_REQUESTCODE && resultCode == RESULT_OK) {
            Log.v(TAG, "Return to Main from Login screen..");
            setUserInfo();

            Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
            startActivityForResult(intent, SELECT_MARKET_REQUESTCODE);

        } else if (requestCode == LOGIN_FOR_PROFILE_REQUESTCODE && resultCode == RESULT_OK) {
            Log.v(TAG, "Return to Main from Login screen..");
            setUserInfo();

            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
            Log.v(TAG, "ProfileActivity is started. OK.");
        }
        else if (requestCode == LOGIN_FOR_ADD_PRODUCT_REQUESTCODE && resultCode == RESULT_OK) {
            Log.v(TAG, "Return to Main from Login screen..");
            setUserInfo();

            Intent intent = new Intent(getBaseContext(), AddProductActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
            Log.v(TAG, "AddProductActivity is started. OK.");
        }
        else if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isInternetAvailable()) {
            Log.v(MainActivity.TAG, "inflating Action bar view..");
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            this.menu = menu;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (user == null)
            menu.findItem(R.id.action_profile).setVisible(false);
        searchMenuItem = menu.findItem(R.id.action_search);
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerPane);
        searchMenuItem.setVisible(!drawerOpen);
        return true;
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(MainActivity.TAG, "actionbar item: " + item.getItemId());
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:
                Log.v(MainActivity.TAG, "Search clicked.");
                openSearchBar();
                break;
            case R.id.action_help:
                // help action
                Log.v(MainActivity.TAG, "HELP");
                return true;
            case R.id.action_settings:
                // check for updates action
                Log.v(MainActivity.TAG, "SETTINGS");
                return true;
            case R.id.action_profile:
                goToProfilePage();
                return true;
        }

        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isInternetAvailable()
    {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Log.d(TAG,"no internet connection");
            return false;
        }
        else
        {
            if (info.isConnected())
            {
                Log.d(TAG," internet connection available...");
                return true;
            }
            else
            {
                Log.d(TAG," internet connection");
                return true;
            }
        }
    }

    public void loadAdImages() {
        mDemoSlider = (SliderLayout)findViewById(R.id.slider);

        HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

        for (String name : url_maps.keySet()) {
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    //.description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);
    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(MainActivity.TAG, this.toString() + " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserInfo();
        if (mSearchOpened) {
            closeSearchBar();
        }
        Log.v(MainActivity.TAG, this.toString() + " onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(MainActivity.TAG, this.toString() + " onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(MainActivity.TAG, this.toString() + " onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(MainActivity.TAG, this.toString() + " onDestroy");
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        mDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setUserInfo() {
        this.loginNameTxt.setText("");
        if (loadUser()) {
            Log.v(TAG, "MainActivity: User is ready.");
            if (this.menu != null)
                this.menu.findItem(R.id.action_profile).setVisible(true);
            loginNameTxt.setText(" full name: " + user.getFirstName() + " " + user.getLastName()
                    + "\n email: " + user.getEmail()
                    + "\n points: " + user.getPoints()
                    + "\n social_type: " + user.getLoginType()
                    + "\n social_id: " + user.getSocial_id()
                    + "\n id: " + user.get_id()
                    + "\n shoplist" + user.getShopLists().toString());
        }
    }

    public boolean loadUser() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String user_str = sp.getString("user", "");
        if (!user_str.equalsIgnoreCase("")) {
            user = gson.fromJson(user_str, User.class);
            Log.v(MainActivity.TAG, "User (" + user.getFirstName() + ") loaded from Shared.");
            return true;
        } else {
            user = null;
        }
        return false;
    }

    public boolean loadMarket() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String str = sp.getString("market", "");
        if( !str.equalsIgnoreCase("") ){
            this.market = gson.fromJson(str, Market.class);
            Log.v(MainActivity.TAG, "Market (" + market.getName() + ") loaded from Shared.");
            return true;
        }
        return false;
    }

    private void openSearchBar() {
        // Set custom view on action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.product_autocomplete_search_bar);

        // Search edit text field setup.
        ac_tv_product_search = (AutoCompleteTextView) actionBar.getCustomView()
                .findViewById(R.id.id_ac_tv_productAutoSearch);
        ac_tv_product_search.setAdapter(new ProductSearchAdapter(this));
        ac_tv_product_search.setOnItemClickListener(this);
        ac_tv_product_search.setText("");
        ac_tv_product_search.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ac_tv_product_search, InputMethodManager.SHOW_FORCED);

        btn_delete_searchTxt = (Button) findViewById(R.id.id_btn_delete);
        btn_delete_searchTxt.setOnClickListener(this);

        // Change searchBox icon accordingly.
        searchMenuItem.setVisible(false);
        mSearchOpened = true;
    }

    private void closeSearchBar() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);

        // Remove custom view.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_main);

        // Change searchBox icon accordingly.
        searchMenuItem.setVisible(true);
        mSearchOpened = false;
    }

    @Override
    public void onClick(View view) {
        // getId() returns this view's identifier.
        if (view.getId() == R.id.id_btn_spy_market) {
            /* Explicit intent for selecting a nearby market */
            Log.v(TAG, "Detective Button clicked. OK.");
            if (user == null) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivityForResult(intent, LOGIN_FOR_SPY_REQUESTCODE);
                Log.v(TAG, "LoginActivity is started. OK.");
            } else {
                Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
                startActivityForResult(intent, SELECT_MARKET_REQUESTCODE);
                Log.v(TAG, "MarketSelectActivity is started. OK.");
            }
        } else if(view.getId() == R.id.id_btn_profile) {
            Log.v(TAG, "Profile Btn is clicked.");
            goToProfilePage();
        } else if (view.getId() == R.id.id_btn_delete) {
            Log.v(MainActivity.TAG, "Delete button clicked: " + ac_tv_product_search.getText());
            if (ac_tv_product_search.getText().toString().equalsIgnoreCase("")) {
                closeSearchBar();
            }
            else {
                ac_tv_product_search.setText("");
            }
        } else if (view.getId() == R.id.id_btn_campaign) {
            Log.v(TAG, "Campaign Btn is clicked.");
        } else if (view.getId() == R.id.id_btn_declare_product) {
            Log.v(TAG, "Declare Product Btn is clicked.");

            if (user == null) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivityForResult(intent, LOGIN_FOR_ADD_PRODUCT_REQUESTCODE);
                Log.v(TAG, "LoginActivity is started. OK.");
            } else {
                Intent intent = new Intent(getBaseContext(), AddProductActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                Log.v(TAG, "AddProductActivity is started. OK.");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Hide keyboard on autocomplete item click
        ac_tv_product_search.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);

        Product p = (Product) adapterView.getItemAtPosition(i);
        Log.v(MainActivity.TAG, "Product searched: " + p.getName());
        ac_tv_product_search.setText(p.getName());

        ArrayList<Product> searchProductList = new ArrayList<Product>();
        searchProductList.add(p);
        searchProductList.add(new Product("Urederm", "8699561460099"));
        searchProductList.add(new Product("Eti Karam 80g", "8690526098043"));

        Intent intent = new Intent(getBaseContext(), SearchResultsActivity.class);
        intent.putParcelableArrayListExtra("searchProductList", searchProductList);
        startActivity(intent);
        Log.v(TAG, "MainActivity: SearchResultsActivity is started. OK.");
    }

    private void goToProfilePage() {
        if (this.user == null) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(intent, LOGIN_FOR_PROFILE_REQUESTCODE);
            Log.v(TAG, "LoginActivity is started. OK.");
        } else {
            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
            startActivity(intent);
            Log.v(TAG, "ProfileActivity is started. OK.");
        }
    }
}
