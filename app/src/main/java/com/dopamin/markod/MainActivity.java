package com.dopamin.markod;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.sqlite.UserDatabaseHandler;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    public static final boolean DEVELOPMENT = true;
    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    private Button searchBtn, detectiveBtn;
    /* Select market request for the Market Select Activity */
    private int SELECT_NEARBY_MARKET_REQUESTCODE = 1;
    public static String MARKET_DETAILS_HASHMAP = "MARKET_DETAILS";

    /* User request for Login Activity */
    private int USER_LOGIN_REQUESTCODE = 2;
    public static String USER_DETAILS = "USER_DETAILS";

    public static String TAG = "MDlog";

    /* Objects */
    public static User user;
    public static Market market;

    private TextView loginNameTxt;
    private TextView marketNameTxt;

    /* SQLite DB */
    private UserDatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
//        if (!MainActivity.DEVELOPMENT)
        user = db.getUser();
        if (user != null) {
            Log.d(TAG, "User: " + user.getId());
            setUserInfo();
        }

        // Enabling Up / Back navigation
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == SELECT_NEARBY_MARKET_REQUESTCODE && resultCode == RESULT_OK && data != null) {
            //num1 = data.getIntExtra(Number1Code);
            //num2 = data.getIntExtra(Number2Code);

            Log.v(TAG, "MainActivity: Market is ready");
            marketNameTxt.setText(market.getPlace_name() + " \nid: " + market.getGmap_id() + " \n"
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
                + "\n social_type: " + user.getUserLoginType().toString()
                + "\n social_id: " + user.getSocial_id()
                + "\n id: " + user.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(MainActivity.TAG, "inflating Action bar view..");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:
                // search action
                Log.v(MainActivity.TAG, "SEARCH");
                return true;
            case R.id.action_location_found:
                // location found
                Log.v(MainActivity.TAG, "FOUND");
                return true;
            case R.id.action_refresh:
                // refresh
                Log.v(MainActivity.TAG, "REFRESH");
                return true;
            case R.id.action_help:
                // help action
                Log.v(MainActivity.TAG, "HELP");
                return true;
            case R.id.action_check_updates:
                // check for updates action
                Log.v(MainActivity.TAG, "UPDATE");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
