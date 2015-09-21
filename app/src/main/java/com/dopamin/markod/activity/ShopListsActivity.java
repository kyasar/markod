package com.dopamin.markod.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONObject;

import java.util.ArrayList;

public class ShopListsActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, AdapterView.OnItemClickListener {

    private ExpandableListAdapter exp_lv_adapter;
    private ExpandableListView exp_lv_shopLists;
    private LinearLayout searchLayout;
    private FrameLayout mainTopLayout;
    private AutoCompleteTextView ac_tv_product_search;
    private Button btn_delete_searchTxt, btn_back, btn_backFromSearch, btn_saveChanges;
    private LinearLayout layout_hintAddShoplist;
    private ProgressDialog progressDialog;

    private User user;
    private int selectedList = -1;
    private boolean mSearchOpened = false;

    public static int SHOPLIST_NAME_DIALOG_FRAGMENT_SUCC_CODE = 700;
    public static int SHOPLIST_NAME_DIALOG_FRAGMENT_FAIL_CODE = 701;

    String userUpdateURL = MainActivity.MDS_SERVER + "/mds/api/user-update/" + "?token=" + MainActivity.MDS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_lists);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_shoplists, null);
        actionBar.setCustomView(v);

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

        btn_saveChanges = (Button) findViewById(R.id.id_btn_save_changes);
        btn_saveChanges.setOnClickListener(this);

        // get the listview
        exp_lv_shopLists = (ExpandableListView) findViewById(R.id.lv_exp_shoplists);

        // setting list adapter
        exp_lv_adapter = new ExpandableListAdapter(this, user.getShopLists());
        exp_lv_shopLists.setAdapter(exp_lv_adapter);
        registerForContextMenu(exp_lv_shopLists);

        /* sending product declaration loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getResources().getString(R.string.str_progress_saving_user_changes));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
                    openSearchBar();
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
            case R.id.home:
                Log.v(MainActivity.TAG, "Return Back clicked.");
                break;
            case R.id.action_settings:
                break;
            case R.id.action_create_new_shoplist:
                showNewShopListNameDialog();
                break;
            case R.id.action_search:
                Log.v(MainActivity.TAG, "Search clicked.");
                openSearchBar();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    private void openSearchBar() {
        // Set custom view on action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.product_autocomplete_search_bar);

        // Search edit text field setup.
        ac_tv_product_search = (AutoCompleteTextView) actionBar.getCustomView()
                .findViewById(R.id.id_ac_tv_productAutoSearch);
        ac_tv_product_search.setAdapter(new ProductSearchAdapter(this));
        ac_tv_product_search.setOnItemClickListener(this);
        ac_tv_product_search.setText("");
        ac_tv_product_search.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ac_tv_product_search, InputMethodManager.SHOW_FORCED);

        btn_delete_searchTxt = (Button) findViewById(R.id.id_btn_delete);
        btn_delete_searchTxt.setOnClickListener(this);

        mSearchOpened = true;
    }

    private void closeSearchBar() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);

        // Remove custom view.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_shoplists);

        // Change search icon accordingly.
        mSearchOpened = false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_back) {
            Log.v(MainActivity.TAG, "finishing shoplist activity.");
            finish();
        } else if (view.getId() == R.id.id_btn_delete) {
            Log.v(MainActivity.TAG, "Delete button clicked: " + ac_tv_product_search.getText());
            if (ac_tv_product_search.getText().toString().equalsIgnoreCase("")) {
                  closeSearchBar();
            }
            else {
                ac_tv_product_search.setText("");
            }
        } else if (view.getId() == R.id.id_btn_save_changes) {
            Log.v(MainActivity.TAG, "Save Changes button clicked. User changes will be sent to server.");
            progressDialog.show();
            Gson gson = new Gson();
            Log.v(MainActivity.TAG, "User: " + gson.toJson(this.user.createJSON_updateShopLists()).toString());

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, userUpdateURL,
                    gson.toJson(this.user.createJSON_updateShopLists()), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("volley", "response: " + response);

                    progressDialog.dismiss();
                    btn_saveChanges.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),
                            "Your activities are saved.", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Log.e(MainActivity.TAG, "Volley: User update error.");
                    Toast.makeText(getApplicationContext(),
                            "Server error ! Try again later..", Toast.LENGTH_SHORT).show();
                }
            });
            //Log.v(MainActivity.TAG, "Sending " + total + " products to Market (" + market.getName() + ") ..");
            Volley.newRequestQueue(getApplication()).add(jsObjRequest);
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
            Toast.makeText(this, "New Shoplist is created: " + value, Toast.LENGTH_SHORT).show();

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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Hide keyboard on autocomplete item click
        ac_tv_product_search.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);

        Product p = (Product) adapterView.getItemAtPosition(i);
        ac_tv_product_search.setText("");

        Log.v(MainActivity.TAG, "Product will be added into Group.");
        if (selectedList >= 0) {
            Log.v(MainActivity.TAG, "Product: " + p.getName() + " will be added into group: " + user.getShopLists().get(selectedList).getName());
            //changeToMainView();
            ShopList shopList = user.getShopLists().get(selectedList);

            // Check whether product is already added into the shoplist or not?
            for (Product pi : shopList.getProducts()) {
                if (pi.getBarcode().equalsIgnoreCase(p.getBarcode())) {
                    Toast.makeText(getApplicationContext(), p.getName() + " " +
                            getResources().getString(R.string.str_toast_product_already_added), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            shopList.getProducts().add(p);
            exp_lv_shopLists.setAdapter(exp_lv_adapter);

            // Make Save button visible
            btn_saveChanges.setVisibility(View.VISIBLE);

            Toast.makeText(getApplicationContext(), p.getName() + " " +
                    getResources().getString(R.string.str_toast_product_added_into_shoplist), Toast.LENGTH_SHORT).show();

            // expand selected group
            exp_lv_shopLists.expandGroup(selectedList);
        }
    }
}
