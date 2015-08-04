package com.dopamin.markod;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.authentication.AsyncLoginResponse;
import com.dopamin.markod.authentication.SignInUp;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.objects.UserLoginType;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;
import java.io.InputStream;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements AsyncLoginResponse,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private CallbackManager callbackManager;
    private LoginButton fbLoginButton;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private SignInUp signInUper;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    private SignInButton gLoginButton;
    private Button btnSignOut, btnRevokeAccess;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private LinearLayout llProfileLayout;
    private static final int RC_SIGN_IN = 0;
    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    String url = MainActivity.MDS_SERVER + "/mds/api/test";



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
                Profile profile = Profile.getCurrentProfile();
                // loginNameTxt.setText(constructWelcomeMessage(profile));

                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                    Log.e(MainActivity.TAG, "facebook graph error");
                                } else {
                                    String email = me.optString("email");
                                    String id = me.optString("id");
                                    // send email and id to your web server
                                    Log.e(MainActivity.TAG, "facebook profile email: " + email + " id: " + id);
                                    txtEmail.setText(email);

                                    new LoadProfileImage(imgProfilePic).execute("https://graph.facebook.com/"
                                            + me.optString("id") + "/picture?type=large");
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

        /////////////////////
        /* Google+ Sign In */
        /////////////////////
        /* Disable Google+ Signin / Login
        gLoginButton = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);

        // Button click listeners
        gLoginButton.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);

        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        */

        // Enabling Up / Back navigation
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.login_title);
        ab.setDisplayShowTitleEnabled(true);
        //ab.setDisplayHomeAsUpEnabled(true);

        signInUper = new SignInUp(this);
        signInUper.delegate = this;

        final GsonRequest gsonRequest = new GsonRequest(url, User.class, null, new Response.Listener<User>() {

            @Override
            public void onResponse(User user) {
                System.out.println("First name: " + user.getFirstName() );
                System.out.println("Last name : " + user.getLastName() );
                System.out.println("ID        : " + user.get_id() );
                System.out.println("Login Type: " + user.getLoginType() );
                System.out.println("Email     : " + user.getEmail() );
                System.out.println("Points    : " + user.getPoints() );
                System.out.println("Social ID : " + user.getSocial_id() );
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(volleyError != null) Log.e("MainActivity", volleyError.getMessage());
            }
        });

        /*JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            Log.v(MainActivity.TAG, "RESPONSE: " + response.toString());

                            System.out.println("First name: " + response.getString("firstName"));
                            System.out.println("Last name : " + response.getString("lastName"));
                            System.out.println("ID        : " + response.getString("_id"));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }); */

        Volley.newRequestQueue(this).add(gsonRequest);
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
                    // loginNameTxt.setText(constructWelcomeMessage(currentProfile));
                    txtName.setText(currentProfile.getName());

                    signInUper.execute(currentProfile.getFirstName(),
                            currentProfile.getLastName(),
                            currentProfile.getMiddleName(),
                            currentProfile.getId(),
                            UserLoginType.FACEBOOK_USER.toString());
                } else {
                    Log.d(MainActivity.TAG, "Profile gone.");
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /* Google+ SigIn Request Code */
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.v(MainActivity.TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void processFinish(boolean res) {
        // Create new async task for reuse
        signInUper = new SignInUp(this);
        signInUper.delegate = this;

        /* SignIn successful and user acquired */
        if (MainActivity.user != null)
            updateUI(true);

        Log.v(MainActivity.TAG, "Login process finished. Return to main activity. res: " + res);
        if (res == false) {
            //TODO: Instead of toast message, an info dialog can be shown for details
            Toast.makeText(this, "Connection or Server problem !\nPlease try again later..", Toast.LENGTH_SHORT).show();
        } else {
            Intent output = new Intent();
            setResult(RESULT_OK, output);
            Toast.makeText(this, "User signup or login: " + MainActivity.user.getSocial_id(), Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        /*if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        getProfileInformation();

        // Update the UI after signin
        updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        updateUI(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /* Disable Google+
            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                // Signout button clicked
                if (MainActivity.user.getUserLoginType() == UserLoginType.GOOGLE_USER)
                    signOutFromGplus();
                else if (MainActivity.user.getUserLoginType() == UserLoginType.FACEBOOK_USER)
                    Log.v(MainActivity.TAG, "Sign out from Facebook");
                break;
            case R.id.btn_revoke_access:
                // Revoke access button clicked
                if (MainActivity.user.getUserLoginType() == UserLoginType.GOOGLE_USER)
                    revokeGplusAccess();
                else if (MainActivity.user.getUserLoginType() == UserLoginType.FACEBOOK_USER)
                    Log.v(MainActivity.TAG, "Revoke access Facebook");
                break;
            */
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            // Make non-visible Login buttons
            //gLoginButton.setVisibility(View.GONE);
            fbLoginButton.setVisibility(View.GONE);

            // Make visible associative logout buttons
            //if (MainActivity.user.getUserLoginType() == UserLoginType.FACEBOOK_USER)
             //   fbLoginButton.setVisibility(View.VISIBLE);
            /*if (MainActivity.user.getUserLoginType() == UserLoginType.GOOGLE_USER) {
                gLoginButton.setVisibility(View.GONE);
                btnSignOut.setVisibility(View.VISIBLE);
                btnRevokeAccess.setVisibility(View.VISIBLE);
            }*/
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            fbLoginButton.setVisibility(View.VISIBLE);
            //gLoginButton.setVisibility(View.VISIBLE);
            //btnSignOut.setVisibility(View.GONE);
            //btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Log.e(MainActivity.TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);

                txtName.setText(personName + " " + currentPerson.getId());
                txtEmail.setText(email);

                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;

                signInUper.execute(currentPerson.getDisplayName(),
                        "",
                        Plus.AccountApi.getAccountName(mGoogleApiClient),
                        currentPerson.getId(),
                        UserLoginType.GOOGLE_USER.toString());

                new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            bmImage.setImageBitmap(result);
        }
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateUI(false);
        }
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.e(MainActivity.TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                            updateUI(false);
                        }
                    });
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
}
