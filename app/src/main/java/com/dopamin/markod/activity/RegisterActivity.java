package com.dopamin.markod.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.dopamin.markod.objects.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_firstName, et_lastName, et_email, et_password;
    TextView tv_firstName, tv_lastName, tv_email, tv_password;
    ActionProcessButton btn_register;
    private Toolbar toolbar;
    private CoordinatorLayout snackbarCoordinatorLayout;

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

        snackbarCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.snackbarCoordinatorLayout);

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
                        String status = null;

                        try {
                            status = response.get("status").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (status != null) {
                            if (status.equalsIgnoreCase("ALREADY")) {
                                snackIt("User is already registered !");
                                setInputs(true);
                            } else if (status.equalsIgnoreCase("NOT_VERIFIED")) {
                                snackIt("Activation link is already sent to your email !");
                                setInputs(true);
                            } else if (status.equalsIgnoreCase("OK")) {
                                snackIt("Activation link sent. Check your email !");
                                showSuccRegisterDialog();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(MainActivity.TAG, "Volley: User register error.");
                        Log.e(MainActivity.TAG, error.toString());
                        btn_register.setProgress(0);
                        setInputs(true);
                        snackIt(getResources().getString(R.string.str_dialog_msg_register_failed));
                    }
                });
        // Set timeout to 15 sec, and try only one time
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplication()).add(jsObjRequest);
    }

    private void setInputs(boolean b) {
        et_firstName.setEnabled(b);
        et_lastName.setEnabled(b);
        et_email.setEnabled(b);
        et_password.setEnabled(b);
        btn_register.setEnabled(b);
    }

    private void clearInputs() {
        et_firstName.setText("");
        et_lastName.setText("");
        et_email.setText("");
        et_password.setText("");
    }

    static final int MSG_DISMISS_DIALOG = 0;
    private AlertDialog dialogRegistered;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    if (dialogRegistered != null && dialogRegistered.isShowing()) {
                        dialogRegistered.dismiss();
                        registrationCompleted();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showSuccRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.str_dialog_msg_register_ok))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        registrationCompleted();
                    }
                });
        dialogRegistered = builder.create();
        dialogRegistered.show();

        // dismiss dialog in TIME_OUT ms
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 4000);
    }

    public void snackIt(String msg) {
        Snackbar.make(snackbarCoordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }

    public void registrationCompleted() {
        finish();
    }
}
