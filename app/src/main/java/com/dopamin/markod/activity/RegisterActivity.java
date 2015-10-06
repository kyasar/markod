package com.dopamin.markod.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.dd.processbutton.iml.ActionProcessButton;
import com.dopamin.markod.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_firstName, et_lastName, et_email, et_password;
    ActionProcessButton btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_firstName = (EditText) findViewById(R.id.id_et_firstname);
        et_lastName = (EditText) findViewById(R.id.id_et_lastname);
        et_email = (EditText) findViewById(R.id.id_et_email);
        et_password = (EditText) findViewById(R.id.id_et_password);

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

    @Override
    public void onClick(View view) {
        // start animation in button
        btn_register.setProgress(1);
        
        et_firstName.setEnabled(false);
        et_lastName.setEnabled(false);
        et_email.setEnabled(false);
        et_password.setEnabled(false);
        btn_register.setEnabled(false);
    }
}
