package com.dopamin.markod.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.R;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.request.GsonRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static User user;
    private CallbackManager callbackManager;
    private LoginButton fbLoginButton;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private LinearLayout llProfileLayout;
    private ProgressDialog progressDialog;
    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;

    private Map<String,String> params = new HashMap<String, String>();

    String url = MainActivity.MDS_SERVER + "/mds/signup/social";

    final GsonRequest gsonRequest = new GsonRequest(Request.Method.POST, url, User.class,
            null, params, new Response.Listener<User>() {

        @Override
        public void onResponse(User user) {
            if (user != null) {
                LoginActivity.user = user;
                System.out.println("First name: " + user.getFirstName());
                System.out.println("Last name : " + user.getLastName());
                System.out.println("ID        : " + user.get_id());
                System.out.println("Login Type: " + user.getLoginType());
                System.out.println("Email     : " + user.getEmail());
                System.out.println("Points    : " + user.getPoints());
                System.out.println("Social ID : " + user.getSocial_id());

                Toast.makeText(getApplicationContext(), "Login success User: "
                        + user.getFirstName(), Toast.LENGTH_SHORT).show();

                // OK, save User into Shared preference
                saveUser(user);
                updateUI(true);
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if(volleyError != null) {
                Log.e("MainActivity", volleyError.getMessage());
                Toast.makeText(getApplicationContext(), "Login failed !!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook Sdk before UI
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        /* Facebook Login */
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.markets_progress);
        progressDialog.setMessage(getResources().getString(R.string.spymarket_progress_message));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        /* These classes call your code when access token or profile changes happen */
        setupTokenTracker();
        setupProfileTracker();

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();

        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                Log.v(MainActivity.TAG, "fblogin onSuccess, token: " + accessToken.getToken()
                        + "\n expires: " + accessToken.getExpires().getHours());

                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                    Log.e(MainActivity.TAG, "facebook graph error");
                                } else {
                                    progressDialog.show();

                                    String email = me.optString("email");
                                    String id = me.optString("id");
                                    String fullName = me.optString("name");

                                    // clear parameters list and then add params
                                    params.clear();
                                    params.put("firstName", me.optString("first_name"));
                                    params.put("lastName", me.optString("last_name"));
                                    params.put("social_id", me.optString("id"));
                                    params.put("loginType", "FACEBOOK");
                                    params.put("email", email);

                                    // send email and id to your web server
                                    Log.e(MainActivity.TAG, "facebook profile email: " + email + ", id: "
                                            + id + ", name: " + fullName );

                                    new LoadProfileImage(imgProfilePic).execute("https://graph.facebook.com/"
                                            + me.optString("id") + "/picture?type=large");

                                    // After adding email to parameters, send the POST login/signup request
                                    Volley.newRequestQueue(getApplication()).add(gsonRequest);
                                }
                            }
                        }).executeAsync();
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

        // Enabling Up / Back navigation
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.login_title);
        ab.setDisplayShowTitleEnabled(true);
    }

    private void setupTokenTracker() {
        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null)
                    Log.d(MainActivity.TAG, "Token changed new token: " + currentAccessToken.getToken());
                else {
                    updateUI(false);
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
                } else {
                    Log.d(MainActivity.TAG, "Profile gone.");
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(MainActivity.TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            // Make non-visible Login buttons
            fbLoginButton.setVisibility(View.GONE);

            // Make visible associative logout buttons
            if (user.getLoginType().equals("FACEBOOK"))
                fbLoginButton.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);

            txtName.setText(user.getFirstName() + " " + user.getLastName());
            txtEmail.setText(user.getEmail());
        } else {
            fbLoginButton.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Log.v(MainActivity.TAG, "Setting profile Image..");
            //Bitmap realImage = BitmapFactory.decodeStream(stream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();

            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            //Log.v(MainActivity.TAG, "Encoded profile image: " + encodedImage);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("profile_image", encodedImage);
            edit.commit();

            bmImage.setImageBitmap(result);
            loginCompleted();
        }
    }

    /* Action Bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(MainActivity.TAG, "inflating Action bar view..");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loginCompleted() {
        progressDialog.dismiss();
        Intent output = new Intent();
        setResult(RESULT_OK, output);
        finish();
    }

    public boolean saveUser(User user) {
        Gson gson = new Gson();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("user", gson.toJson(user));
        Log.v(MainActivity.TAG, "User saved into Shared.");
        return edit.commit();
    }
}
