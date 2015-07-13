package com.dopamin.markod;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class BaseActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(MainActivity.TAG, "inflating Action bar view..");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
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
            case R.id.action_search:
                // search action
                Log.v(MainActivity.TAG, "SEARCH");
                return true;
            case R.id.action_location_found:
                // location found
                Log.v(MainActivity.TAG, "FOUND");
                return true;
            case R.id.action_refresh:
                // refresh
                Log.v(MainActivity.TAG, "REFRESH");
                return true;
            case R.id.action_help:
                // help action
                Log.v(MainActivity.TAG, "HELP");
                return true;
            case R.id.action_check_updates:
                // check for updates action
                Log.v(MainActivity.TAG, "UPDATE");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
