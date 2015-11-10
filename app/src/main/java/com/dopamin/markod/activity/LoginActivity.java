package com.dopamin.markod.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dd.processbutton.iml.ActionProcessButton;
import com.dopamin.markod.R;
import com.dopamin.markod.objects.ConnectionDetector;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONException;
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

    ActionProcessButton btn_login;
    EditText et_email, et_password;
    TextView tv_email, tv_password;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private ProgressDialog progressDialog;
    // Profile pic image size in pixels

    private Toolbar toolbar;
    private CoordinatorLayout snackbarCoordinatorLayout;

    private TextView link_to_register;

    private Map<String,String> params = new HashMap<String, String>();

    private ConnectionDetector cd;

    String socialLoginURL = MainActivity.MDS_SERVER + "/mds/signup/social";
    String localLoginURL = MainActivity.MDS_SERVER + "/mds/signup/login/";

    final GsonRequest gsonRequest = new GsonRequest(Request.Method.POST, socialLoginURL, User.class,
            null, params, new Response.Listener<User>() {

        @Override
        public void onResponse(User user) {
            if (user != null) {
                LoginActivity.user = user;

                // OK, save User into Shared preference
                // saveUser(user);
                new LoadProfileImage().execute("https://graph.facebook.com/"
                        + user.getSocial_id() + "/picture?type=large");

                updateUI(true);
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if(volleyError != null) {
                // TODO: this line cause to "println needs a message" error - fix it later for log.
                //Log.e("MainActivity", volleyError.getMessage());
                snackIt(getResources().getString(R.string.str_msg_login_failed));
                progressDialog.dismiss();
                callFacebookLogout();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook Sdk before UI
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        snackbarCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.snackbarCoordinatorLayout);
        cd = new ConnectionDetector(getApplicationContext());

        /* Facebook Login */
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        et_email = (EditText) findViewById(R.id.id_et_email);
        et_password = (EditText) findViewById(R.id.id_et_password);

        tv_email = (TextView) findViewById(R.id.id_tv_email_err);
        tv_password = (TextView) findViewById(R.id.id_tv_password_err);

        btn_login = (ActionProcessButton) findViewById(R.id.id_btn_login);
        btn_login.setMode(ActionProcessButton.Mode.ENDLESS);
        btn_login.setOnClickListener(this);

        link_to_register = (TextView) findViewById(R.id.link_to_register);
        link_to_register.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.dialog_login_title);
        progressDialog.setMessage(getResources().getString(R.string.dialog_login_msg));
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

                                    // After adding email to parameters, send the POST login/signup request
                                    // Set timeout to 15 sec, and try only one time
                                    gsonRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                                            1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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

    /**
     * Logout From Facebook
     */
    public static void callFacebookLogout() {
        LoginManager.getInstance().logOut();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        } else {
            fbLoginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.link_to_register) {
            link_to_register.setPaintFlags(link_to_register.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

            if (!cd.isConnectingToInternet()) {
                snackIt(getResources().getString(R.string.str_err_no_conn));
                return;
            } else {
                Intent intent = new Intent(getBaseContext(), RegisterActivity.class);
                startActivity(intent);
                Log.v(MainActivity.TAG, "RegisterActivity is started. OK.");
            }
        } else if (view.getId() == R.id.id_btn_login) {
            Log.v(MainActivity.TAG, "Logging in with credentials..");
            loginLocally();
        }
    }

    /**
     * Background Async task to load user profile picture from socialLoginURL
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {

        public LoadProfileImage() {
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
            LoginActivity.user.setEncodedProfilePhoto(encodedImage);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loginCompleted() {
        Log.v(MainActivity.TAG, "Login Completed, saving user.. ");
        saveUser(this.user);
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

    // Local Login
    public static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean checkInputs() {
        boolean r = true;

        if (!isEmailValid(et_email.getText().toString().trim())) {
            tv_email.setVisibility(View.VISIBLE);
            r = false;
        } else {
            tv_email.setVisibility(View.INVISIBLE);
        }

        if (et_password.getText().toString().trim().length() < 4) {
            tv_password.setVisibility(View.VISIBLE);
            r = false;
        } else {
            tv_password.setVisibility(View.INVISIBLE);
        }

        return r;
    }

    private void setInputs(boolean b) {
        et_email.setEnabled(b);
        et_password.setEnabled(b);
        btn_login.setEnabled(b);
    }

    private void loginLocally() {
        if (!checkInputs()) {
            return;
        }

        if (!cd.isConnectingToInternet()) {
            snackIt(getResources().getString(R.string.str_err_no_conn));
            return;
        }

        btn_login.setProgress(1);
        setInputs(false);

        User queryUser = new User();
        queryUser.setEmail(et_email.getText().toString().trim());
        queryUser.setPassword(et_password.getText().toString().trim());
        queryUser.setLoginType("LOCAL");

        final Gson gson = new Gson();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, localLoginURL
                + "?api_key=" + MainActivity.MDS_API_KEY,
                gson.toJson(queryUser), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("volley", "response: " + response);
                String status = null;

                try {
                    status = response.get("status").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status != null) {
                    if (status.equalsIgnoreCase("NOT_VERIFIED")) {
                        snackIt(getResources().getString(R.string.str_dialog_msg_not_verified_yet));
                    } else if (status.equalsIgnoreCase("OK")) {
                        try {
                            user = gson.fromJson(response.getJSONObject("user").toString(), User.class);
                            Log.v(MainActivity.TAG, "User OK: " + user.getFirstName());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            snackIt(getResources().getString(R.string.str_msg_err_server));
                        }
                        snackIt(getResources().getString(R.string.str_dialog_msg_login_ok));
                        showSuccLoginDialog();
                    } else if (status.equalsIgnoreCase("NO_USER")) {
                        setInputs(true);
                        snackIt(getResources().getString(R.string.str_dialog_msg_no_such_user));
                    }
                    else {
                        setInputs(true);
                        snackIt(getResources().getString(R.string.str_dialog_msg_login_error));
                    }
                }
                setInputs(true);
                btn_login.setProgress(0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MainActivity.TAG, "Volley: User login error.");
                Log.e(MainActivity.TAG, error.toString());
                btn_login.setProgress(0);
                setInputs(true);
                snackIt(getResources().getString(R.string.str_dialog_msg_login_error));
            }
        });

        // Set timeout to 15 sec, and try only one time
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplication()).add(jsObjRequest);
    }

    static final int MSG_DISMISS_DIALOG = 0;
    private android.support.v7.app.AlertDialog dialogSuccLogin;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    if (dialogSuccLogin != null && dialogSuccLogin.isShowing()) {
                        dialogSuccLogin.dismiss();
                        loginCompleted();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showSuccLoginDialog() {
        // Stop button animation
        btn_login.setProgress(0);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        //builder.setTitle(getResources().getString(R.string.str_dialog_msg_login_succ_title));
        builder.setPositiveButton(getResources().getString(R.string.str_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loginCompleted();
                    }
                });
        //builder.setNegativeButton("Cancel", null);
        builder.setMessage(getResources().getString(R.string.str_dialog_msg_login_ok));
        dialogSuccLogin = builder.create();
        dialogSuccLogin.show();

        // dismiss dialog in TIME_OUT ms
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 4000);
    }

    public void snackIt(String msg) {
        Snackbar.make(snackbarCoordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }


}
