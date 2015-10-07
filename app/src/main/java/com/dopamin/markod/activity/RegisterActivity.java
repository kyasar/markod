package com.dopamin.markod.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dd.processbutton.iml.ActionProcessButton;
import com.dopamin.markod.R;
import com.dopamin.markod.objects.User;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_firstName, et_lastName, et_email, et_password;
    TextView tv_firstName, tv_lastName, tv_email, tv_password;
    ActionProcessButton btn_register;
    private Toolbar toolbar;

    String userRegisterURL = MainActivity.MDS_SERVER + "/mds/signup/local/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_firstName = (EditText) findViewById(R.id.id_et_firstname);
        et_lastName = (EditText) findViewById(R.id.id_et_lastname);
        et_email = (EditText) findViewById(R.id.id_et_email);
        et_password = (EditText) findViewById(R.id.id_et_password);

        tv_firstName = (TextView) findViewById(R.id.id_tv_firstname_err);
        tv_lastName = (TextView) findViewById(R.id.id_tv_lastname_err);
        tv_email = (TextView) findViewById(R.id.id_tv_email_err);
        tv_password = (TextView) findViewById(R.id.id_tv_password_err);

        btn_register = (ActionProcessButton) findViewById(R.id.id_btn_register);
        btn_register.setMode(ActionProcessButton.Mode.ENDLESS);
        btn_register.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    public static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean checkInputs() {
        boolean r = true;
        if (et_firstName.getText().toString().trim().length() < 2) {
            tv_firstName.setVisibility(View.VISIBLE);
            r = false;
        } else {
            tv_firstName.setVisibility(View.INVISIBLE);
        }

        if (et_lastName.getText().toString().trim().length() < 2) {
            tv_lastName.setVisibility(View.VISIBLE);
            r = false;
        } else {
            tv_lastName.setVisibility(View.INVISIBLE);
        }

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

    @Override
    public void onClick(View view) {
        // start animation in button

        if (!checkInputs()) {
            return;
        }

        btn_register.setProgress(1);
        setInputs(false);

        User user = new User();
        user.setFirstName(et_firstName.getText().toString().trim());
        user.setLastName(et_lastName.getText().toString().trim());
        user.setEmail(et_email.getText().toString().trim());
        user.setPassword(et_password.getText().toString().trim());
        user.setLoginType("LOCAL");

        Gson gson = new Gson();
        Log.v(MainActivity.TAG, "User: " + gson.toJson(user).toString());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, userRegisterURL,
                gson.toJson(user), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("volley", "Response: " + response);
                        btn_register.setProgress(0);
                        setInputs(true);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(MainActivity.TAG, "Volley: User register error.");
                        btn_register.setProgress(0);
                        setInputs(true);
                    }
                });
        Volley.newRequestQueue(getApplication()).add(jsObjRequest);
    }

    private void setInputs(boolean b) {
        et_firstName.setEnabled(b);
        et_lastName.setEnabled(b);
        et_email.setEnabled(b);
        et_password.setEnabled(b);
        btn_register.setEnabled(b);
    }
}
