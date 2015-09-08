package com.dopamin.markod.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
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
import java.util.List;

public class MainActivity extends FragmentActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    public static final String MDS_TOKEN = "test";
    public static boolean internetConn = false;
    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    public static String MDS_SERVER = "http://192.168.1.21:8000";

    private Button deleteBtn, detectiveBtn, connCheckBtn;

    /* Select market request for the Market Select Activity */
    private int SELECT_NEARBY_MARKET_REQUESTCODE = 1;

    /* User request for Login Activity */
    private int USER_LOGIN_REQUESTCODE = 2;

    public static String TAG = "MDlog";

    /* Objects */
    public static User user;
    public static Market market;

    private TextView loginNameTxt;
    private TextView marketNameTxt;
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
            connCheckBtn = (Button) findViewById(R.id.checkConn_button);
            connCheckBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    internetConn = isInternetAvailable();
                }
            });
            return;
        }
        setContentView(R.layout.activity_main);

        deleteBtn = (Button) findViewById(R.id.id_btn_delete);
        detectiveBtn = (Button) findViewById(R.id.detective_button);
        loginNameTxt = (TextView) findViewById(R.id.login_name_text);
        marketNameTxt = (TextView) findViewById(R.id.market_name_text);

        detectiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				/* Explicit intent for selecting a nearby market */
                Log.v(TAG, "Detective Button clicked. OK.");
                if (user == null) {
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivityForResult(intent, USER_LOGIN_REQUESTCODE);
                    Log.v(TAG, "LoginActivity is started. OK.");
                } else {
                    Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
                    startActivityForResult(intent, SELECT_NEARBY_MARKET_REQUESTCODE);
                    Log.v(TAG, "MarketSelectActivity is started. OK.");
                }
            }
        });

        /* Load Location-based Ads */
        loadAdImages();

        /* Search product cancel text button */
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ac_tv_product_search.setText("");
            }
        });

        ac_tv_product_search = (AutoCompleteTextView) findViewById(R.id.id_ac_tv_productAutoSearch);
        ac_tv_product_search.setAdapter(new ProductSearchAdapter(this));
        ac_tv_product_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product p = (Product) adapterView.getItemAtPosition(i);
                Log.v(MainActivity.TAG, "Product searched: " + p.getName());
                ac_tv_product_search.setText(p.getName());

                ArrayList<Product> searchProductList = new ArrayList<Product>();
                searchProductList.add(p);
                searchProductList.add(new Product("Urederm", "8699561460099"));

                Intent intent = new Intent(getBaseContext(), SearchResultsActivity.class);
                intent.putParcelableArrayListExtra("searchProductList", searchProductList);
                startActivity(intent);
                Log.v(TAG, "MainActivity: SearchResultsActivity is started. OK.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == SELECT_NEARBY_MARKET_REQUESTCODE && resultCode == RESULT_OK && data != null) {

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

        } else if (requestCode == USER_LOGIN_REQUESTCODE && resultCode == RESULT_OK) {
            Log.v(TAG, "Return to Main from Login screen..");
            setUserInfo();

            Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
            startActivityForResult(intent, SELECT_NEARBY_MARKET_REQUESTCODE);
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
        if (loadUser()) {
            Log.v(TAG, "MainActivity: User is ready.");
            if (this.menu != null)
                this.menu.findItem(R.id.action_profile).setVisible(true);
            loginNameTxt.setText(" full name: " + user.getFirstName() + " " + user.getLastName()
                    + "\n email: " + user.getEmail()
                    + "\n points: " + user.getPoints()
                    + "\n social_type: " + user.getLoginType()
                    + "\n social_id: " + user.getSocial_id()
                    + "\n id: " + user.get_id());
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
}
