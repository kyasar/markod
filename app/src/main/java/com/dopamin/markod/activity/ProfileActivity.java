package com.dopamin.markod.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.objects.User;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgProfilePic;
    private TextView txtName, txtEmail, txtPoints;
    private Button btn_shoplists;
    private LoginButton fbLoginButton;
    private ProfileTracker mProfileTracker;

    private Toolbar toolbar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook Sdk before UI
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_profile);

        if (loadUser() == false) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.str_toast_no_valid_user), Toast.LENGTH_SHORT).show();
            Log.e(MainActivity.TAG, "No valid user in app !!");
            finish();
        }

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtPoints = (TextView) findViewById(R.id.txtPoints);
        imgProfilePic = (ImageView) findViewById(R.id.id_profile_image);
        btn_shoplists = (Button) findViewById(R.id.id_btn_shoplists);
        btn_shoplists.setOnClickListener(this);

        if (user.getEncodedProfilePhoto() != null) {
            byte[] b = Base64.decode(user.getEncodedProfilePhoto(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            imgProfilePic.setImageBitmap(bitmap);
        }

        txtName.setText(user.getFirstName() + " " + user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPoints.setText("Points: " + user.getPoints());

        if (user.getLoginType().equalsIgnoreCase("FACEBOOK")) {
            fbLoginButton.setVisibility(View.VISIBLE);
            setupProfileTracker();
            mProfileTracker.startTracking();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_shoplists) {
            Intent intent = new Intent(getBaseContext(), ShopListsActivity.class);
            startActivity(intent);
            Log.v(MainActivity.TAG, "ShopList Activity is started. OK.");
        }
    }

    public boolean saveUser(User user) {
        Gson gson = new Gson();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sp.edit();
        if (user == null) {
            edit.remove("user");
            Log.v(MainActivity.TAG, "User removed.");
        } else {
            edit.putString("user", gson.toJson(user));
            Log.v(MainActivity.TAG, "User saved into Shared.");
        }
        return edit.commit();
    }

    private void setupProfileTracker() {
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile == null) {
                    Log.d(MainActivity.TAG, "Profile gone.");
                    user = null;
                    saveUser(user);
                    finish();
                }
            }
        };
    }

    public boolean loadUser() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String user_str = sp.getString("user", "");
        if (!user_str.equalsIgnoreCase("")) {
            this.user = gson.fromJson(user_str, User.class);
            Log.v(MainActivity.TAG, "User (" + user.getFirstName() + ") loaded from Shared.");
            return true;
        }
        this.user = null;
        return false;
    }
}
