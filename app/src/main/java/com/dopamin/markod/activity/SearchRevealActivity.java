package com.dopamin.markod.activity;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.R;
import com.dopamin.markod.search.SearchBox;
import com.dopamin.markod.search.SearchResult;
import com.dopamin.markod.objects.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchRevealActivity extends AppCompatActivity {

    private SearchBox searchBox;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_reveal);

        getString(R.string.app_name);
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        searchBox.enableVoiceRecognition(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                searchBox.toggleSearch();
                return true;
            }
        });
        openSearch();
        searchBox.requestFocus();
        searchBox.toggleSearch();
    }

    public void getProducts(final String search) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                MainActivity.MDS_SERVER + "/mds/api/products" + "?search=" + search,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MainActivity.TAG, response.toString());

                        try {
                            // TODO: Json respond check status?
                            // Parsing json array response
                            // loop through each json object
                            JSONArray jsonProducts = response.getJSONArray("product");

                            Log.v(MainActivity.TAG, "Product searchBox array respond length: " + jsonProducts.length());
                            searchBox.clearResults();
                            searchBox.getSearchables().clear();
                            for (int i = 0; i < jsonProducts.length(); i++) {
                                JSONObject p = (JSONObject) jsonProducts.get(i);
                                String name = p.getString("name");
                                String barcode = p.getString("barcode");
                                Product product = new Product(name, barcode);

                                SearchResult option = new SearchResult(product.getName(),
                                        getResources().getDrawable(R.drawable.ico_points));
                                searchBox.addSearchable(option);
                            }
                            searchBox.updateResults();
                            searchBox.showLoading(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(req);
    }

    public void openSearch() {
        toolbar.setTitle("");
        searchBox.setHintText(getResources().getString(R.string.str_hint_product_searchbox));
        searchBox.revealFromMenuItem(R.id.action_search, this);
        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                Toast.makeText(SearchRevealActivity.this, "Menu click", Toast.LENGTH_LONG).show();
                searchBox.toggleSearch();
            }
        });
        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                Log.v(MainActivity.TAG, "Search Opened.");
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                Log.v(MainActivity.TAG, "Search Closed.");
                searchBox.clearResults();
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the searchBox term changing
                //Called after it has updated results
                Log.v(MainActivity.TAG, "TERM: " + term);
                searchBox.showLoading(true);
                getProducts(term);
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(SearchRevealActivity.this, searchTerm + " Searched", Toast.LENGTH_LONG).show();
                Log.v(MainActivity.TAG, "Search DONE.");
            }

            @Override
            public void onSearchCleared() {
                Log.v(MainActivity.TAG, "Search Cleared.");
                //Called when the clear button is clicked
            }

            @Override
            public void onResultItemClicked(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                Log.v(MainActivity.TAG, "Search Item Clicked: " + searchBox.getSearchables().get(pos).title);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void closeSearch() {
        searchBox.hideCircularly(this);
        if (searchBox.getSearchText().isEmpty())toolbar.setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_reveal, menu);
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
}
