package com.dopamin.markod.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener,
        View.OnClickListener {

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
    private ImageView btn_searchImage;
    private LinearLayout searchFrameLayout;
    private RelativeLayout mainTopLayout;
    private AutoCompleteTextView ac_tv_product_search;

    private SliderLayout mDemoSlider;
    private Menu menu;

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

        // Main actionbar layout Search Image button
        btn_searchImage = (ImageView) findViewById(R.id.id_search_image_btn);
        btn_searchImage.setOnClickListener(this);

        // Search frame layout Back and Delete text buttons
        btn_backMain = (Button) findViewById(R.id.id_btn_back_from_search);
        btn_backMain.setOnClickListener(this);

        btn_delete_searchTxt = (Button) findViewById(R.id.id_btn_delete);
        btn_delete_searchTxt.setOnClickListener(this);

        // Main Menu buttons: Spy, Declare, Campaign and Profile
        btn_spy_market = (Button) findViewById(R.id.id_btn_spy_market);
        btn_spy_market.setOnClickListener(this);

        btn_profile = (Button) findViewById(R.id.id_btn_profile);
        btn_profile.setOnClickListener(this);

        btn_campaign = (Button) findViewById(R.id.id_btn_campaign);
        btn_campaign.setOnClickListener(this);

        btn_declare_product = (Button) findViewById(R.id.id_btn_declare_product);
        btn_declare_product.setOnClickListener(this);

        // Layouts to change search state or main state
        loginNameTxt = (TextView) findViewById(R.id.login_name_text);
        marketNameTxt = (TextView) findViewById(R.id.market_name_text);
        searchFrameLayout = (LinearLayout) findViewById(R.id.search_frame);
        mainTopLayout = (RelativeLayout) findViewById(R.id.main_top_layout);

        /* Load Location-based Ads */
        loadAdImages();

        ac_tv_product_search = (AutoCompleteTextView) findViewById(R.id.id_ac_tv_productAutoSearch);
        ac_tv_product_search.setAdapter(new ProductSearchAdapter(this));
        ac_tv_product_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });

        ac_tv_product_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.v(MainActivity.TAG, "On focus: " + b);
            }
        });
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
        Log.v(MainActivity.TAG, "");
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
            case R.id.action_help:
                // help action
                Log.v(MainActivity.TAG, "HELP");
                return true;
            case R.id.action_settings:
                // check for updates action
                Log.v(MainActivity.TAG, "SETTINGS");
                return true;
            case R.id.action_profile:
                Log.v(MainActivity.TAG, "PROFILE");
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                Log.v(MainActivity.TAG, "Profile activity started.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public User createMockUser() {
        return new User("0123456789", "Mock", "Mockish", "mock@mock.com",
                "FACEBOOK", "3333a3333", 55);
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
        ac_tv_product_search.setText("");
        searchFrameLayout.setVisibility(View.GONE);
        mainTopLayout.setVisibility(View.VISIBLE);
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

    private void changeToSearchView() {
        searchFrameLayout.setVisibility(View.VISIBLE);
        mainTopLayout.setVisibility(View.GONE);
        ac_tv_product_search.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ac_tv_product_search, InputMethodManager.SHOW_IMPLICIT);
    }

    private void changeToMainView() {
        searchFrameLayout.setVisibility(View.GONE);
        mainTopLayout.setVisibility(View.VISIBLE);
        ac_tv_product_search.clearFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);
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
            if (user == null) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivityForResult(intent, LOGIN_FOR_PROFILE_REQUESTCODE);
                Log.v(TAG, "LoginActivity is started. OK.");
            } else {
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                startActivity(intent);
                Log.v(TAG, "ProfileActivity is started. OK.");
            }
        } else if (view.getId() == R.id.id_btn_delete) {
            Log.v(MainActivity.TAG, "Delete button clicked: " + ac_tv_product_search.getText());
            if (ac_tv_product_search.getText().toString().equalsIgnoreCase("")) {
                changeToMainView();
            }
            else {
                ac_tv_product_search.setText("");
            }
        } else if (view.getId() == R.id.id_btn_back_from_search) {
            changeToMainView();
        } else if (view.getId() == R.id.id_search_image_btn) {
            changeToSearchView();
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
}
