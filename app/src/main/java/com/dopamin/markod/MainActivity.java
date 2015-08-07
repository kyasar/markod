package com.dopamin.markod;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.sqlite.UserDatabaseHandler;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    public static final String MDS_TOKEN = "test";
    public static boolean internetConn = false;
    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    public static String MDS_SERVER = "http://192.168.43.120:8000";

    private Button searchBtn, detectiveBtn, connCheckBtn;

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

    /* SQLite DB */
    private UserDatabaseHandler db;

    private SliderLayout mDemoSlider;

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

        db = new UserDatabaseHandler(this);

        searchBtn = (Button) findViewById(R.id.search_button);
        detectiveBtn = (Button) findViewById(R.id.detective_button);
        loginNameTxt = (TextView) findViewById(R.id.login_name_text);
        marketNameTxt = (TextView) findViewById(R.id.market_name_text);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v(TAG, "Search Button clicked. OK.");
            }
        });

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

        // Reading the user info from db
        Log.d(TAG, "Reading User from DB..");
        // if (!MainActivity.DEVELOPMENT)
        //user = db.getUser();
        user = createMockUser();
        if (user != null) {
            //Log.d(TAG, "User: " + user.getId());
            setUserInfo();
        }

        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.main_title);

        loadAdImages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == SELECT_NEARBY_MARKET_REQUESTCODE && resultCode == RESULT_OK && data != null) {
            //num1 = data.getIntExtra(Number1Code);
            //num2 = data.getIntExtra(Number2Code);

            Log.v(TAG, "MainActivity: Market is ready");
            marketNameTxt.setText(market.getName() + " \nid: " + market.getId() + " \n"
                    + market.getVicinity());

            Intent intent = new Intent(getBaseContext(), SpyMarketActivity.class);
            startActivity(intent);
            Log.v(TAG, "MainActivity: SpyMarketActivity is started. OK.");
        } else if (requestCode == USER_LOGIN_REQUESTCODE && resultCode == RESULT_OK) {

            setUserInfo();

            // Inserting Contacts
            Log.d(TAG, "Inserting User ..");
            db.addUser(user);

            Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
            startActivityForResult(intent, SELECT_NEARBY_MARKET_REQUESTCODE);
            Log.v(TAG, "MarketSelectActivity is started. OK.");
        }
    }

    private void setUserInfo() {
        Log.v(TAG, "MainActivity: User is ready.");
        loginNameTxt.setText(" full name: " + user.getFirstName() + " " + user.getLastName()
                + "\n email: " + user.getEmail()
                + "\n points: " + user.getPoints()
                //+ "\n social_type: " + user.getUserLoginType().toString()
                + "\n social_id: " + user.getSocial_id());
                //+ "\n id: " + user.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isInternetAvailable()) {
            Log.v(MainActivity.TAG, "inflating Action bar view..");
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else {
            return false;
        }
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
                // search action
                Log.v(MainActivity.TAG, "SEARCH");
                return true;
            case R.id.action_refresh:
                // refresh
                Log.v(MainActivity.TAG, "REFRESH");
                return true;
            case R.id.action_help:
                // help action
                Log.v(MainActivity.TAG, "HELP");
                return true;
            case R.id.action_settings:
                // check for updates action
                Log.v(MainActivity.TAG, "SETTINGS");
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
                "FACEBOOK", "3333a3333", 0);
    }
}
