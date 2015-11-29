package com.dopamin.markod.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.dopamin.markod.R;
import com.dopamin.markod.adapter.ViewPagerAdapter;
import com.dopamin.markod.objects.*;
import com.google.gson.Gson;
import com.dopamin.markod.search.SearchBox;

import java.util.ArrayList;
import java.util.HashMap;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener,
        View.OnClickListener {

    public static final String MDS_TOKEN = "test";
    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    public static final String MDS_API_KEY = "15b240618c19d0cb36d0778d52253177";
    public static String MDS_SERVER = "http://192.168.1.24:8000";

    private Button btn_spy_market;

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

    // Toolbar and Navigation Drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;

    private SearchBox searchBox;

    private AutoScrollViewPager ads_viewpager;

    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private CoordinatorLayout snackbarCoordinatorLayout;
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snackbarCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.snackbarCoordinatorLayout);
        cd = new ConnectionDetector(getApplicationContext());

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(null);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set the menu icon instead of the launcher icon.
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Find and setup drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.setDrawerListener(drawerToggle);

        // Main Menu buttons: Spy, Declare, Campaign and Profile
        btn_spy_market = (Button) findViewById(R.id.id_btn_spy_market);
        btn_spy_market.setOnClickListener(this);

        /* Load Location-based Ads */
        //loadAdImages();

        // Search Box
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        searchBox.enableVoiceRecognition(this);
        searchBox.setLogoText(getResources().getString(R.string.app_name));
        searchBox.setHintText(getResources().getString(R.string.str_hint_product_searchbox));

        ads_viewpager = (AutoScrollViewPager) findViewById(R.id.id_ads_pager);
        int [] icons = {R.drawable.ic_action_search, R.drawable.ic_account_circle_black_24dp, R.drawable.ico_scan_market};
        String [] ads = {   getResources().getString(R.string.str_ads_search),
                            getResources().getString(R.string.str_ads_account),
                            getResources().getString(R.string.str_ads_spy) };
        final ViewPagerAdapter ads_adapter = new ViewPagerAdapter(this, ads, icons);
        ads_viewpager.setAdapter(ads_adapter);

        ads_viewpager.setCycle(true);
        ads_viewpager.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_CYCLE);
        ads_viewpager.setInterval(3000);
        ads_viewpager.setScrollDurationFactor(2);
        ads_viewpager.startAutoScroll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == SELECT_MARKET_REQUESTCODE && resultCode == RESULT_OK && data != null) {
            Log.v(TAG, "MainActivity: Market is ready");
            loadMarket();
            loadUser();

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
        if (cd.isConnectingToInternet()) {
            Log.v(MainActivity.TAG, "inflating Action bar view..");
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_search:
                Log.v(MainActivity.TAG, "Search clicked.");
                openSearch();
                //startActivity(new Intent(this, SearchRevealActivity.class));
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

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*public void loadAdImages() {
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
    }*/

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
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setUserInfo() {
        // Set profile info in Navigation Header
        updateNavProfile(loadUser());
    }

    private void updateNavProfile(boolean online) {
        View profileBox = findViewById(R.id.id_profileBox);
        View tv_welcome = findViewById(R.id.id_tv_welcome);

        if (online) {
            profileBox.setVisibility(View.VISIBLE);
            tv_welcome.setVisibility(View.GONE);

            CircleImageView profileImage = (CircleImageView) profileBox.findViewById(R.id.id_profile_image);
            TextView profileName = (TextView) profileBox.findViewById(R.id.id_profile_userName);
            TextView profilePoints = (TextView) profileBox.findViewById(R.id.id_profile_points);

            if (user.getEncodedProfilePhoto() != null) {
                byte[] b = Base64.decode(user.getEncodedProfilePhoto(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            }
            profileName.setText(user.getFullName());
            profilePoints.setText(user.getPoints() + " " + getResources().getString(R.string.str_points));

            nvDrawer.getMenu().findItem(R.id.nav_login).setVisible(false);
        } else {
            profileBox.setVisibility(View.GONE);
            tv_welcome.setVisibility(View.VISIBLE);

            nvDrawer.getMenu().findItem(R.id.nav_login).setVisible(true);
            nvDrawer.getMenu().findItem(R.id.nav_declare_product).setVisible(false);
            nvDrawer.getMenu().findItem(R.id.nav_profile).setVisible(false);
            nvDrawer.getMenu().findItem(R.id.nav_shoplists).setVisible(false);
            nvDrawer.getMenu().findItem(R.id.nav_favorites).setVisible(false);
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

    @Override
    public void onClick(View view) {
        // getId() returns this view's identifier.
        if (view.getId() == R.id.id_btn_spy_market) {
            // Check internet conn at first
            if (!cd.isConnectingToInternet()) {
                snackIt(getResources().getString(R.string.str_err_no_conn));
                return;
            }

            if (user == null) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivityForResult(intent, LOGIN_FOR_SPY_REQUESTCODE);
                Log.v(TAG, "LoginActivity is started. OK.");
            } else {
                Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
                startActivityForResult(intent, SELECT_MARKET_REQUESTCODE);
                Log.v(TAG, "MarketSelectActivity is started. OK.");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.v(MainActivity.TAG, "Back pressed !");
            if (searchBox.getSearchOpen()) {
                closeSearch();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        switch(menuItem.getItemId()) {
            case R.id.nav_profile:
            case R.id.nav_login:
                goToProfilePage();
                break;
            case R.id.nav_shoplists:
                goToShopListsFavorites();
                break;
            case R.id.nav_favorites:
                goToShopListsFavorites();
                break;
            case R.id.nav_declare_product:
                goToDeclareProduct();
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_help_feedback:
                break;
            default:
        }

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        //setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    private void goToDeclareProduct() {
        Log.v(TAG, "Declare Product Btn is clicked.");

        if (user != null) {
            Intent intent = new Intent(getBaseContext(), AddProductActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
            Log.v(TAG, "AddProductActivity is started. OK.");
        }
    }

    public void openSearch() {
        searchBox.setVisibility(View.VISIBLE);
        searchBox.revealFromMenuItem(R.id.action_search, this);
        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                //searchBox.toggleSearch();
            }
        });
        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                Log.v(MainActivity.TAG, "Search Opened.");
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                Log.v(MainActivity.TAG, "Search Closed.");
                searchBox.clearResults();
                if (searchBox.isSearchOpen())
                    closeSearch();
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the searchBox term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                Log.v(MainActivity.TAG, "Search DONE.");
            }

            @Override
            public void onSearchCleared() {
                Log.v(MainActivity.TAG, "Search Cleared.");
                //Called when the clear button is clicked
                searchBox.setSearchString("");
            }

            @Override
            public void onResultItemClicked(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                Product quickSearchProduct = searchBox.getSearchables().get(pos).getProduct();
                Log.v(MainActivity.TAG, "Product: " + quickSearchProduct.getName() + " is being searched..");
                searchNearbyMarkets(quickSearchProduct);
            }
        });
    }

    protected void closeSearch() {
        searchBox.hideCircularly(this);
        toolbar.setTitle(getResources().getString(R.string.app_name));
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

    private void goToShopListsFavorites() {
        if (this.user != null) {
            Intent intent = new Intent(getBaseContext(), ShopListsActivity.class);
            startActivity(intent);
            Log.v(MainActivity.TAG, "ShopList Activity is started. OK.");
        }
    }

    private void searchNearbyMarkets(Product searchProduct) {
        ArrayList<Product> searchProductList = new ArrayList<Product>();
        searchProductList.add(searchProduct);

        Intent intent = new Intent(getBaseContext(), SearchResultsActivity.class);
        intent.putParcelableArrayListExtra("searchProductList", searchProductList);
        startActivity(intent);
        Log.v(MainActivity.TAG, "ShopListActivity: SearchResultsActivity is started. OK.");
    }

    public void snackIt(String msg) {
        Snackbar.make(snackbarCoordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }
}
