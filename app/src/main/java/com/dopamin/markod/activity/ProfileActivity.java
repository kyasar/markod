package com.dopamin.markod.activity;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.objects.User;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePic;
    private TextView txtName, txtEmail, txtPoints;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.v(MainActivity.TAG, ProfileActivity.this.toString() + " onCreate");

        Bundle bundle = getIntent().getExtras();
        User user = (User) bundle.getSerializable("user");

        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtPoints = (TextView) findViewById(R.id.txtPoints);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);

        byte[] b = Base64.decode(user.getEncodedProfilePhoto(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        imgProfilePic.setImageBitmap(bitmap);

        txtName.setText(user.getFirstName() + " " + user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPoints.setText("Points: " + user.getPoints());
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
}
