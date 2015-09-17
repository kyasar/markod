package com.dopamin.markod.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.adapter.ExpandableListAdapter;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.ShopList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopListsActivity extends Activity implements View.OnClickListener, TextWatcher/*, AdapterView.OnItemLongClickListener*/ {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private Button btn_newShopList;
    private String listName;
    AlertDialog newListNameDialog;

    private List<ShopList> shopLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_lists);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lv_exp_shoplists);

        btn_newShopList = (Button) findViewById(R.id.id_btn_new_shoplist);
        btn_newShopList.setOnClickListener(this);

        // preparing list data
        prepareListData();

        // setting list adapter
        //listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter = new ExpandableListAdapter(this, shopLists);
        expListView.setAdapter(listAdapter);
        //expListView.setOnItemLongClickListener(this);
        registerForContextMenu(expListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_shoplist, menu);

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Show context menu for groups
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            Log.v(MainActivity.TAG, "Group: " + groupPosition );
            menu.setHeaderTitle(shopLists.get(groupPosition).getName());
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
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

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            switch(item.getItemId()) {
                case R.id.id_menu_shoplist_search:
                    Log.v(MainActivity.TAG, "Searching ShopList " + shopLists.get(groupPosition).getName());
                    break;
                case R.id.id_menu_shoplist_delete:
                    Log.v(MainActivity.TAG, "Deleting ShopList " + shopLists.get(groupPosition).getName());
                    deleteShopList(groupPosition);
                    break;
            }
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            switch(item.getItemId()) {
                case R.id.id_menu_shoplist_search:
                    Log.v(MainActivity.TAG, "Searching A Product " + shopLists.get(groupPosition).getProducts().get(childPosition).getName());
                    break;
                case R.id.id_menu_shoplist_delete:
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_new_shoplist) {
            openNewShopListDialog();
        }
    }

    public void openNewShopListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.str_dialog_title_newShoplist));

        LinearLayout ll = new LinearLayout(this);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(llParams);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(40, 20, 40, 20);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setLayoutParams(llParams);
        input.setPadding(20, 10, 20, 10);
        input.addTextChangedListener(this);

        ll.addView(input);
        builder.setView(ll);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shopLists.add(new ShopList(input.getText().toString().trim()));
                expListView.setAdapter(listAdapter);
                Log.v(MainActivity.TAG, "NEw Shoplist name: " + listName);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Log.v(MainActivity.TAG, "NEw Shoplist Canceled ");
            }
        });
        newListNameDialog = builder.create();

        // At first, OK is disabled as no name entered.
        //newListNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        newListNameDialog.show();
        newListNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //newListNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.toString().trim().length() >= 6) {
            newListNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            newListNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void removeItemFromList(int group, int child) {
        this.shopLists.get(group).getProducts().remove(child);
        expListView.setAdapter(listAdapter);
    }

    private void deleteShopList(int group) {
        this.shopLists.remove(group);
        expListView.setAdapter(listAdapter);
    }
}
