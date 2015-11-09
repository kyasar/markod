package com.dopamin.markod.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.dd.processbutton.iml.ActionProcessButton;
import com.dopamin.markod.objects.TokenManager;
import com.dopamin.markod.objects.TokenResult;
import com.dopamin.markod.search.SearchBox;
import com.google.gson.Gson;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.R;
import com.dopamin.markod.adapter.*;
import com.dopamin.markod.dialog.ShopListNameDialogFragment;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.ShopList;
import com.dopamin.markod.objects.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShopListsActivity extends AppCompatActivity implements View.OnClickListener, TokenResult {

    private ExpandableListAdapter exp_lv_adapter;
    private ExpandableListView exp_lv_shopLists;
    private ActionProcessButton btn_saveChanges;
    private LinearLayout layout_hintAddShoplist;
    private ProgressDialog progressDialog;

    private User user;
    private TokenManager tm;
    private int selectedList = -1;

    // ShopList expandable listview cannot be clickable while saving changes
    private boolean isListClickable = true;

    private Toolbar toolbar;
    private SearchBox searchBox;

    CoordinatorLayout snackbarCoordinatorLayout;

    public static int SHOPLIST_NAME_DIALOG_FRAGMENT_SUCC_CODE = 700;
    public static int SHOPLIST_NAME_DIALOG_FRAGMENT_FAIL_CODE = 701;

    String userUpdateURL = MainActivity.MDS_SERVER + "/mds/api/user-update/" + "?token=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_lists);

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layout_hintAddShoplist = (LinearLayout) findViewById(R.id.id_layout_hint_create_shoplists);

        if (loadUser() == false) {
            Log.e(MainActivity.TAG, "No valid user in app !!");
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.str_toast_no_valid_user), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // User found but It has no Shoplist so Show hint to create
            if (user.getShopLists().size() == 0) {
                layout_hintAddShoplist.setVisibility(View.VISIBLE);
            }
        }

        snackbarCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.snackbarCoordinatorLayout);

        btn_saveChanges = (ActionProcessButton) findViewById(R.id.id_btn_save_changes);
        btn_saveChanges.setMode(ActionProcessButton.Mode.ENDLESS);
        btn_saveChanges.setOnClickListener(this);

        // get the listview
        exp_lv_shopLists = (ExpandableListView) findViewById(R.id.lv_exp_shoplists);

        // This necessary to stop auto divider in expandable listview
        exp_lv_shopLists.setDividerHeight(0);

        // setting list adapter
        exp_lv_adapter = new ExpandableListAdapter(this, user.getShopLists());
        exp_lv_shopLists.setAdapter(exp_lv_adapter);
        registerForContextMenu(exp_lv_shopLists);

        tm = new TokenManager(getApplicationContext());
        // Result will be returned to this Activity
        tm.delegateTokenResult = this;

        /* sending product declaration loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getResources().getString(R.string.str_progress_saving_user_changes));

        // Search Box
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        searchBox.enableVoiceRecognition(this);
        searchBox.setLogoText(getResources().getString(R.string.app_name));
        searchBox.setHintText(getResources().getString(R.string.str_hint_product_searchbox));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (!isListClickable)
            return;

        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        MenuInflater inflater = getMenuInflater();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Show context menu for groups
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            inflater.inflate(R.menu.context_shoplist, menu);
            Log.v(MainActivity.TAG, "Group: " + groupPosition );
            menu.setHeaderTitle(user.getShopLists().get(groupPosition).getName());
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            inflater.inflate(R.menu.context_shoplist_item, menu);
            Log.v(MainActivity.TAG, "Group: " + groupPosition + ", Child: " + childPosition);
            menu.setHeaderTitle(user.getShopLists().get(groupPosition).getProducts().get(childPosition).getName());
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!isListClickable)
            return false;

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
                .getMenuInfo();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        selectedList = groupPosition;

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            switch(item.getItemId()) {
                case R.id.id_menu_shoplist_search:
                    Log.v(MainActivity.TAG, "Searching ShopList " + user.getShopLists().get(groupPosition).getName());
                    searchNearbyMarkets((ArrayList<Product>) user.getShopLists().get(groupPosition).getProducts());
                    break;
                case R.id.id_menu_shoplist_delete:
                    Log.v(MainActivity.TAG, "Deleting ShopList " + user.getShopLists().get(groupPosition).getName());
                    deleteShopList();
                    break;
                case R.id.id_menu_add_product:
                    Log.v(MainActivity.TAG, "Adding a Product to ShopList " + user.getShopLists().get(groupPosition).getName());
                    openSearch();
                    break;
            }
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            switch(item.getItemId()) {
                case R.id.id_menu_shoplist_item_search:
                    Log.v(MainActivity.TAG, "Searching A Product " + user.getShopLists().get(groupPosition).getProducts().get(childPosition).getName());
                    searchNearbyMarkets(user.getShopLists().get(groupPosition).getProducts().get(childPosition));
                    break;
                case R.id.id_menu_shoplist_item_delete:
                    Log.v(MainActivity.TAG, "Removing A Product " + user.getShopLists().get(groupPosition).getProducts().get(childPosition).getName());
                    removeItemFromList(groupPosition, childPosition);
                    break;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shop_lists, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch(item.getItemId()) {
            case R.id.action_search:
                Log.v(MainActivity.TAG, "Search clicked.");
                openSearch();
                break;
            case R.id.action_settings:
                break;
            case R.id.action_create_new_shoplist:
                showNewShopListNameDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    private void showNewShopListNameDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ShopListNameDialogFragment alertDialog = ShopListNameDialogFragment.newInstance(
                getResources().getString(R.string.str_dialog_title_newShoplist));
        ft.add(alertDialog, "fragment_alert");
        // prevent data loss from screen rotates
        ft.commitAllowingStateLoss();
    }

    private void removeItemFromList(int group, int child) {
        this.user.getShopLists().get(group).getProducts().remove(child);
        exp_lv_shopLists.setAdapter(exp_lv_adapter);
        exp_lv_shopLists.expandGroup(group);
        btn_saveChanges.setVisibility(View.VISIBLE);
        snackIt("Product is removed from shoplist");
    }

    private boolean isProductAlreadyAdded(int group, Product p) {
        List<Product> products = this.user.getShopLists().get(group).getProducts();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getBarcode().matches(p.getBarcode())) {
                Log.v(MainActivity.TAG, "The product " + p.getBarcode()
                        + " is already added. Just updating the price.");
                return true;
            }
        }
        return false;
    }

    private void addItemToList(int group, Product p) {
        if (!isProductAlreadyAdded(group, p)) {
            this.user.getShopLists().get(group).getProducts().add(p);
            exp_lv_shopLists.setAdapter(exp_lv_adapter);
            exp_lv_shopLists.expandGroup(group);
            btn_saveChanges.setVisibility(View.VISIBLE);
        }
    }

    private void deleteShopList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.str_dialog_title_delete_shoplist)
                + " " + user.getShopLists().get(selectedList).getName() + " ?");
        builder.setMessage(R.string.str_are_you_sure);

        builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                user.getShopLists().remove(selectedList);
                exp_lv_shopLists.setAdapter(exp_lv_adapter);
                if (user.getShopLists().size() == 0) {
                    layout_hintAddShoplist.setVisibility(View.VISIBLE);
                }
                btn_saveChanges.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendJSONObjectRequest() {
        Log.v(MainActivity.TAG, "Save Changes button clicked. User changes will be sent to server.");
        //progressDialog.show();

        btn_saveChanges.setProgress(1);
        btn_saveChanges.setClickable(false);
        isListClickable = false;

        Gson gson = new Gson();
        Log.v(MainActivity.TAG, "User: " + gson.toJson(this.user.createJSON_updateShopLists()).toString());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, userUpdateURL + tm.getCurrentToken()
                + "&api_key=" + MainActivity.MDS_API_KEY,
                gson.toJson(this.user.createJSON_updateShopLists()), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(MainActivity.TAG, "Response: " + response);
                String status = null;

                try {
                    status = response.get("status").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status != null) {
                    if (status.equalsIgnoreCase("OK")) {
                        btn_saveChanges.setProgress(0);
                        btn_saveChanges.setClickable(true);
                        isListClickable = true;
                        btn_saveChanges.setVisibility(View.GONE);
                        snackIt(getResources().getString(R.string.str_msg_changes_saved));
                    }
                    else if (status.equalsIgnoreCase("EXPIRED") || status.equalsIgnoreCase("NOTOKEN")) {
                        Log.v(MainActivity.TAG, "Token expired or not provided");
                        tm.getToken(user);
                        Log.v(MainActivity.TAG, "New Token is being waited..");
                    }
                    else {
                        btn_saveChanges.setProgress(0);
                        btn_saveChanges.setClickable(true);
                        isListClickable = true;
                        Log.v(MainActivity.TAG, getResources().getString(R.string.str_msg_err_server));
                        snackIt(getResources().getString(R.string.str_msg_err_server));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //progressDialog.dismiss();
                btn_saveChanges.setProgress(0);
                btn_saveChanges.setClickable(true);
                isListClickable = true;

                Log.e(MainActivity.TAG, "Volley: User update error.");
                snackIt(getResources().getString(R.string.str_msg_err_server));
            }
        });

        // Set timeout to 15 sec, and try only one time
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getApplication()).add(jsObjRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save user to shared
        saveUser(this.user);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_save_changes) {
            sendJSONObjectRequest();
        }
    }

    /* Price Fragment Dialog Select Button Listener */
    public void onUserSelectValue(int code, String value) {
        // TODO Auto-generated method stub
        if (code == SHOPLIST_NAME_DIALOG_FRAGMENT_SUCC_CODE) {
            String listName = value.trim();
            user.getShopLists().add(new ShopList(listName));
            exp_lv_shopLists.setAdapter(exp_lv_adapter);
            btn_saveChanges.setVisibility(View.VISIBLE);
            layout_hintAddShoplist.setVisibility(View.GONE);
            Log.v(MainActivity.TAG, "New Shoplist name: " + listName);
            snackIt("New Shoplist is created: " + value);
        } else if (code == SHOPLIST_NAME_DIALOG_FRAGMENT_FAIL_CODE) {
            Log.v(MainActivity.TAG, "New Shoplist creation Canceled ");
        }
    }

    private void searchNearbyMarkets(ArrayList<Product> searchProductList) {
        Intent intent = new Intent(getBaseContext(), SearchResultsActivity.class);
        intent.putParcelableArrayListExtra("searchProductList", searchProductList);
        startActivity(intent);
        Log.v(MainActivity.TAG, "ShopListActivity: SearchResultsActivity is started. OK.");
    }

    private void searchNearbyMarkets(Product searchProduct) {
        ArrayList<Product> searchProductList = new ArrayList<Product>();
        searchProductList.add(searchProduct);

        Intent intent = new Intent(getBaseContext(), SearchResultsActivity.class);
        intent.putParcelableArrayListExtra("searchProductList", searchProductList);
        startActivity(intent);
        Log.v(MainActivity.TAG, "ShopListActivity: SearchResultsActivity is started. OK.");
    }

    public boolean saveUser(User user) {
        Gson gson = new Gson();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("user", gson.toJson(user));
        Log.v(MainActivity.TAG, "User saved into Shared.");
        return edit.commit();
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

    public void openSearch() {
        toolbar.setTitle("");
        searchBox.setVisibility(View.VISIBLE);
        searchBox.revealFromMenuItem(R.id.action_search, this);
        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                //searchBox.toggleSearch();
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
                if (searchBox.isSearchOpen())
                    closeSearch();
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the searchBox term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                Log.v(MainActivity.TAG, "Search DONE.");
            }

            @Override
            public void onSearchCleared() {
                Log.v(MainActivity.TAG, "Search Cleared.");
                //Called when the clear button is clicked
            }

            @Override
            public void onResultItemClicked(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                Log.v(MainActivity.TAG, "Search Item Clicked: " + searchBox.getSearchables().get(pos).getProduct().getName());
                addItemToList(selectedList, searchBox.getSearchables().get(pos).getProduct());

                //Use this to un-tint the screen
                searchBox.clearResults();
                searchBox.clearSearchable();
                searchBox.setSearchString("");
                if (searchBox.isSearchOpen())
                    closeSearch();
            }
        });
        searchBox.requestFocus();
    }

    protected void closeSearch() {
        searchBox.hideCircularly(this);
        toolbar.setTitle(getResources().getString(R.string.title_activity_shop_lists));
    }

    public void snackIt(String msg) {
        Snackbar.make(snackbarCoordinatorLayout, msg, Snackbar.LENGTH_SHORT)
            .setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
    }

    public void searchShopList(int groupPosition) {
        if (isListClickable) {
            searchNearbyMarkets((ArrayList<Product>) user.getShopLists().get(groupPosition).getProducts());
        }
    }

    public void addToShopList(int groupPosition) {
        if (isListClickable) {
            selectedList = groupPosition;
            openSearch();
        }
    }

    @Override
    public void tokenSuccess(String token) {
        Log.v(MainActivity.TAG, "Token SUCCESS: " + token);
        // retry request
        sendJSONObjectRequest();
    }

    @Override
    public void tokenExpired() {

    }

    @Override
    public void tokenFailed() {

    }
}
