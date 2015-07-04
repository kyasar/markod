package com.dopamin.markod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dopamin.markod.objects.User;

import java.util.HashMap;

public class MainActivity extends Activity {

    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    private Button searchBtn, detectiveBtn;
    private int SELECT_NEARBY_MARKET_REQUESTCODE = 1;
    public static String MARKET_DETAILS_HASHMAP = "MARKET_DETAILS";
    private int USER_LOGIN_REQUESTCODE = 2;
    public static String USER_DETAILS = "USER_DETAILS";
    public static String TAG = "MDlog";
    public static User user;
    private TextView loginNameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBtn = (Button) findViewById(R.id.search_button);
        detectiveBtn = (Button) findViewById(R.id.detective_button);
        loginNameTxt = (TextView) findViewById(R.id.login_name_text);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == SELECT_NEARBY_MARKET_REQUESTCODE && resultCode == RESULT_OK && data != null) {
            //num1 = data.getIntExtra(Number1Code);
            //num2 = data.getIntExtra(Number2Code);

            HashMap<String, String> market = (HashMap<String, String>) data.getExtras().getSerializable(MARKET_DETAILS_HASHMAP);

            Log.v(TAG, "MainActivity: selected market name: " + market.get("place_name").toString());
            Log.v(TAG, "MainActivity: selected market   ID: " + market.get("place_id").toString());

            Intent intent = new Intent(getBaseContext(), SpyMarketActivity.class);
            startActivity(intent);
            Log.v(TAG, "MainActivity: SpyMarketActivity is started. OK.");
        } else if (requestCode == USER_LOGIN_REQUESTCODE && resultCode == RESULT_OK) {

            Log.v(TAG, "MainActivity: User is ready.");
            loginNameTxt.setText(user.getFirstName()+ " " + user.getLastName() + " " + user.getPoints());

            Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
            startActivityForResult(intent, SELECT_NEARBY_MARKET_REQUESTCODE);
            Log.v(TAG, "MarketSelectActivity is started. OK.");
        }
    }

}
