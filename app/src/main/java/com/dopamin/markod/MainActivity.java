package com.dopamin.markod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dopamin.markod.authentication.FacebookSignup;
import com.dopamin.markod.objects.User;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.HashMap;

public class MainActivity extends Activity {

    public static final String GOOGLE_API_KEY = "AIzaSyAsNF78R8Xfd63JsdSJD9RP22X7M7o_0sE";
    private Button searchBtn, detectiveBtn;
    private int SELECT_NEARBY_MARKET_REQUESTCODE = 1;
    public static String MARKET_DETAILS_HASHMAP = "MARKET_DETAILS";
    public static String TAG = "MDlog";
    public static User user;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView loginNameTxt;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook Sdk before UI
        FacebookSdk.sdkInitialize(getApplicationContext());

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
                Intent intent = new Intent(getBaseContext(), MarketSelectActivity.class);
                startActivityForResult(intent, SELECT_NEARBY_MARKET_REQUESTCODE);
                Log.v(TAG, "MainActivity: MarketSelectActivity is started. OK.");
            }
        });

        /* Facebook Login */
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

        /* These classes call your code when access token or profile changes happen */
        setupTokenTracker();
        setupProfileTracker();

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();

        /* This is dev code; gets current profile info even if app shuts down */
        setProfileTextView();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                Log.v(TAG, "fblogin onSuccess, token: " + accessToken.getToken());
                Profile profile = Profile.getCurrentProfile();
                loginNameTxt.setText(constructWelcomeMessage(profile));
            }

            @Override
            public void onCancel() {
                // App code
                Log.v(TAG, "fblogin onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v(TAG, "fblogin onError");
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
        }
        else { /* Facebook Activity returns */
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupTokenTracker() {
        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null)
                    Log.d(TAG, "Token changed new token: " + currentAccessToken.getToken());
                else {
                    Log.d(TAG, "Logout request.");
                }
            }
        };
    }

    private void setupProfileTracker() {
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    Log.d(TAG, "Profile changed new name: " + currentProfile.getName());
                    Log.d(TAG, "Profile changed token: " + AccessToken.getCurrentAccessToken().getToken());
                    loginNameTxt.setText(constructWelcomeMessage(currentProfile));

                    FacebookSignup fs = new FacebookSignup();
                    fs.execute(currentProfile.getFirstName(), currentProfile.getLastName(), currentProfile.getMiddleName(),
                            currentProfile.getId(), AccessToken.getCurrentAccessToken().getToken());
                } else {
                    Log.d(TAG, "Profile gone.");
                }
            }
        };
    }

    private String constructWelcomeMessage(Profile profile) {
        StringBuffer stringBuffer = new StringBuffer();
        if (profile != null) {
            stringBuffer.append("Welcome " + profile.getName());
        }
        return stringBuffer.toString();
    }

    private void setProfileTextView() {
        loginNameTxt.setText(constructWelcomeMessage(Profile.getCurrentProfile()));
    }
}
