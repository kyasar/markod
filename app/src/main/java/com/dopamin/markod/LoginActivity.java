package com.dopamin.markod;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dopamin.markod.authentication.AsyncLoginResponse;
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


public class LoginActivity extends Activity implements AsyncLoginResponse {

    public static String MDS_SERVER = "http://192.168.1.23:8000";
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView loginNameTxt;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    FacebookSignup fs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook Sdk before UI
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        /* Facebook Login */
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        fs = new FacebookSignup(this);
        fs.delegate = this;

        /* These classes call your code when access token or profile changes happen */
        setupTokenTracker();
        setupProfileTracker();

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();

        /* This is dev code; gets current profile info even if app shuts down */
        //setProfileTextView();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                Log.v(MainActivity.TAG, "fblogin onSuccess, token: " + accessToken.getToken());
                Profile profile = Profile.getCurrentProfile();
                // loginNameTxt.setText(constructWelcomeMessage(profile));
            }

            @Override
            public void onCancel() {
                // App code
                Log.v(MainActivity.TAG, "fblogin onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v(MainActivity.TAG, "fblogin onError");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTokenTracker() {
        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null)
                    Log.d(MainActivity.TAG, "Token changed new token: " + currentAccessToken.getToken());
                else {
                    Log.d(MainActivity.TAG, "Logout request.");
                }
            }
        };
    }

    private void setupProfileTracker() {
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    Log.d(MainActivity.TAG, "Profile changed new name: " + currentProfile.getName());
                    Log.d(MainActivity.TAG, "Profile changed token: " + AccessToken.getCurrentAccessToken().getToken());
                    // loginNameTxt.setText(constructWelcomeMessage(currentProfile));

                    fs.execute(currentProfile.getFirstName(), currentProfile.getLastName(), currentProfile.getMiddleName(),
                            currentProfile.getId(), AccessToken.getCurrentAccessToken().getToken());
                } else {
                    Log.d(MainActivity.TAG, "Profile gone.");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(MainActivity.TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void processFinish(boolean res) {
        Log.v(MainActivity.TAG, "Login process finished. Return to main activity. res: " + res);
        if (res == false) {
            //TODO: Instead of toast message, an info dialog can be shown for details
            Toast.makeText(this, "Connection or Server problem !\nPlease try again later..", Toast.LENGTH_SHORT).show();
        } else {
            Intent output = new Intent();
            setResult(RESULT_OK, output);
            finish();
        }
    }
}
