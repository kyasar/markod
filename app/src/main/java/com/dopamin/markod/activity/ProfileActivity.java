package com.dopamin.markod.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.objects.User;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

public class ProfileActivity extends Activity implements View.OnClickListener {

    private ImageView imgProfilePic;
    private TextView txtName, txtEmail, txtPoints;
    private Button btn_back;
    private LoginButton fbLoginButton;
    private ProfileTracker mProfileTracker;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook Sdk before UI
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();
        user = bundle.getParcelable("user");

        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtPoints = (TextView) findViewById(R.id.txtPoints);
        imgProfilePic = (ImageView) findViewById(R.id.id_profile_image);
        btn_back = (Button) findViewById(R.id.id_btn_back);
        btn_back.setOnClickListener(this);

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
    protected void onStart() {
        super.onStart();
        Log.v(MainActivity.TAG, ProfileActivity.this.toString() + " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(MainActivity.TAG, ProfileActivity.this.toString() + " onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(MainActivity.TAG, ProfileActivity.this.toString() + " onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(MainActivity.TAG, ProfileActivity.this.toString() + " onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(MainActivity.TAG, ProfileActivity.this.toString() + " onDestroy");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_back) {
            Log.v(MainActivity.TAG, "Return back to Main menu..");
            finish();
        }
    }

    public boolean saveUser(User user) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
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
}
