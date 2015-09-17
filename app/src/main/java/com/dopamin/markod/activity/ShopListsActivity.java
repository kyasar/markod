package com.dopamin.markod.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
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
import android.widget.ListView;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.adapter.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopListsActivity extends Activity implements View.OnClickListener/*, AdapterView.OnItemLongClickListener*/ {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private Button btn_newShopList;
    private String listName;

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
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
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
            menu.setHeaderTitle(listDataHeader.get(groupPosition));
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            Log.v(MainActivity.TAG, "Group: " + groupPosition + ", Child: " + childPosition);
            menu.setHeaderTitle(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));
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
                    Log.v(MainActivity.TAG, "Searching ShopList " + listDataHeader.get(groupPosition));
                    break;
                case R.id.id_menu_shoplist_delete:
                    Log.v(MainActivity.TAG, "Deleting ShopList " + listDataHeader.get(groupPosition));
                    break;
            }
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            switch(item.getItemId()) {
                case R.id.id_menu_shoplist_search:
                    Log.v(MainActivity.TAG, "Searching A Product " + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));
                    break;
                case R.id.id_menu_shoplist_delete:
                    Log.v(MainActivity.TAG, "Removing A Product " + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));
                    break;
            }
        }

        return super.onContextItemSelected(item);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Top 250");
        listDataHeader.add("Now Showing");
        listDataHeader.add("Coming Soon..");

        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);
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

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(20, 10, 20, 10);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listName = input.getText().toString();
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

        builder.show();
    }

    /*@Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            int itemType = ExpandableListView.getPackedPositionType(id);

            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int childPosition = ExpandableListView.getPackedPositionChild(id);
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);

                Log.v(MainActivity.TAG, "Group: " + groupPosition + ", Child: " + childPosition);
                //do your per-item callback here
                return true; //true if we consumed the click, false if not

            } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                Log.v(MainActivity.TAG, "Group: " + groupPosition);
                //do your per-group callback here
                return true; //true if we consumed the click, false if not

            } else {
                // null item; we don't consume the click
                return false;
            }
    }*/
}
