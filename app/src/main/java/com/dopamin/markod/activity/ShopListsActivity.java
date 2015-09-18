package com.dopamin.markod.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.adapter.*;
import com.dopamin.markod.dialog.ShopListNameDialogFragment;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.ShopList;

import java.util.ArrayList;
import java.util.List;

public class ShopListsActivity extends FragmentActivity implements View.OnClickListener  {

    private ExpandableListAdapter exp_lv_adapter;
    private ExpandableListView exp_lv_shopLists;
    private LinearLayout searchLayout;
    private FrameLayout mainTopLayout;
    private AutoCompleteTextView ac_tv_product_search;
    private Button btn_delete_searchTxt, btn_back, btn_backFromSearch;

    private List<ShopList> shopLists;
    private int selectedList = -1;

    public static int SHOPLIST_NAME_DIALOG_FRAGMENT_SUCC_CODE = 700;
    public static int SHOPLIST_NAME_DIALOG_FRAGMENT_FAIL_CODE = 701;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_lists);

        searchLayout = (LinearLayout) findViewById(R.id.id_layout_search);
        mainTopLayout = (FrameLayout) findViewById(R.id.id_layout_top);

        btn_delete_searchTxt = (Button) findViewById(R.id.id_btn_delete);
        btn_delete_searchTxt.setOnClickListener(this);

        btn_back = (Button) findViewById(R.id.id_btn_back);
        btn_back.setOnClickListener(this);

        btn_backFromSearch = (Button) findViewById(R.id.id_btn_back_from_search);
        btn_backFromSearch.setOnClickListener(this);

        // get the listview
        exp_lv_shopLists = (ExpandableListView) findViewById(R.id.lv_exp_shoplists);

        // preparing list data
        prepareListData();

        // setting list adapter
        exp_lv_adapter = new ExpandableListAdapter(this, shopLists);
        exp_lv_shopLists.setAdapter(exp_lv_adapter);
        registerForContextMenu(exp_lv_shopLists);

        ac_tv_product_search = (AutoCompleteTextView) findViewById(R.id.id_ac_tv_productAutoSearch);
        ac_tv_product_search.setAdapter(new ProductSearchAdapter(this));
        ac_tv_product_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Hide keyboard on autocomplete item click
                ac_tv_product_search.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);

                Product p = (Product) adapterView.getItemAtPosition(i);
                ac_tv_product_search.setText("");

                if (selectedList >= 0) {
                    Log.v(MainActivity.TAG, "Product: " + p.getName() + " will be added into group: " + shopLists.get(selectedList).getName());
                    changeToMainView();
                    ShopList shopList = shopLists.get(selectedList);

                    // Check whether product is already added into the shoplist or not?
                    for(Product pi : shopList.getProducts()) {
                        if (pi.getBarcode().equalsIgnoreCase(p.getBarcode())) {
                            Toast.makeText(getApplicationContext(), p.getName() + " " +
                                    getResources().getString(R.string.str_toast_product_already_added), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    shopList.getProducts().add(p);
                    exp_lv_shopLists.setAdapter(exp_lv_adapter);

                    Toast.makeText(getApplicationContext(), p.getName() + " " +
                            getResources().getString(R.string.str_toast_product_added_into_shoplist), Toast.LENGTH_SHORT).show();

                    // expand selected group
                    exp_lv_shopLists.expandGroup(selectedList);
                }
            }
        });
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
            menu.setHeaderTitle(shopLists.get(groupPosition).getName());
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            inflater.inflate(R.menu.context_shoplist_item, menu);
            Log.v(MainActivity.TAG, "Group: " + groupPosition + ", Child: " + childPosition);
            menu.setHeaderTitle(shopLists.get(groupPosition).getProducts().get(childPosition).getName());
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
                    Log.v(MainActivity.TAG, "Searching ShopList " + shopLists.get(groupPosition).getName());
                    break;
                case R.id.id_menu_shoplist_delete:
                    Log.v(MainActivity.TAG, "Deleting ShopList " + shopLists.get(groupPosition).getName());
                    deleteShopList();
                    break;
                case R.id.id_menu_add_product:
                    Log.v(MainActivity.TAG, "Adding a Product to ShopList " + shopLists.get(groupPosition).getName());
                    changeToSearchView();
                    break;
            }
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            switch(item.getItemId()) {
                case R.id.id_menu_shoplist_item_search:
                    Log.v(MainActivity.TAG, "Searching A Product " + shopLists.get(groupPosition).getProducts().get(childPosition).getName());
                    break;
                case R.id.id_menu_shoplist_item_delete:
                    Log.v(MainActivity.TAG, "Removing A Product " + shopLists.get(groupPosition).getProducts().get(childPosition).getName());
                    removeItemFromList(groupPosition, childPosition);
                    break;
            }
        }

        return super.onContextItemSelected(item);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {

        ShopList sl1 = new ShopList("Home Needs 1");
        List<Product> products = new ArrayList<Product>();
        products.add(new Product("Product 1", "473847389"));
        products.add(new Product("Product 2", "333333339"));
        products.add(new Product("Product 3", "888888889"));
        products.add(new Product("Product 4", "222222229"));
        sl1.setProducts(products);

        ShopList sl2 = new ShopList("Children-Kids");
        List<Product> products2 = new ArrayList<Product>();
        products2.add(new Product("Product 11", "473847389"));
        products2.add(new Product("Product 22", "333333339"));
        products2.add(new Product("Product 33", "888888889"));
        products2.add(new Product("Product 44", "222222229"));
        sl2.setProducts(products2);

        shopLists = new ArrayList<ShopList>();
        shopLists.add(sl1);
        shopLists.add(sl2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shop_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch(item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_create_new_shoplist:
                showNewShopListNameDialog();
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
        this.shopLists.get(group).getProducts().remove(child);
        exp_lv_shopLists.setAdapter(exp_lv_adapter);
        exp_lv_shopLists.expandGroup(group);
    }

    private void deleteShopList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.str_dialog_title_delete_shoplist)
                + " " + shopLists.get(selectedList).getName() + " ?");
        builder.setMessage(R.string.str_are_you_sure);

        builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                shopLists.remove(selectedList);
                exp_lv_shopLists.setAdapter(exp_lv_adapter);
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
        changeToMainView();
    }

    private void changeToSearchView() {
        searchLayout.setVisibility(View.VISIBLE);
        mainTopLayout.setVisibility(View.GONE);
        ac_tv_product_search.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ac_tv_product_search, InputMethodManager.SHOW_IMPLICIT);
    }

    private void changeToMainView() {
        searchLayout.setVisibility(View.GONE);
        mainTopLayout.setVisibility(View.VISIBLE);
        ac_tv_product_search.clearFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_tv_product_search.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_back_from_search) {
            changeToMainView();
        } else if (view.getId() == R.id.id_btn_back) {
            Log.v(MainActivity.TAG, "finishing shoplist activity.");
            finish();
        } else if (view.getId() == R.id.id_btn_delete) {
            Log.v(MainActivity.TAG, "Delete button clicked: " + ac_tv_product_search.getText());
            if (ac_tv_product_search.getText().toString().equalsIgnoreCase("")) {
                changeToMainView();
            }
            else {
                ac_tv_product_search.setText("");
            }
        }
    }

    /* Price Fragment Dialog Select Button Listener */
    public void onUserSelectValue(int code, String value) {
        // TODO Auto-generated method stub
        if (code == SHOPLIST_NAME_DIALOG_FRAGMENT_SUCC_CODE) {
            String listName = value.trim();
            shopLists.add(new ShopList(listName));
            exp_lv_shopLists.setAdapter(exp_lv_adapter);
            Log.v(MainActivity.TAG, "New Shoplist name: " + listName);
            Toast.makeText(this, "New Shoplist is created: " + value, Toast.LENGTH_SHORT).show();

        } else if (code == SHOPLIST_NAME_DIALOG_FRAGMENT_FAIL_CODE) {
            Log.v(MainActivity.TAG, "New Shoplist creation Canceled ");
        }
    }

}
