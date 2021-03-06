package com.dopamin.markod.adapter;

/**
 * Created by kadir on 16.09.2015.
 */
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.activity.ShopListsActivity;
import com.dopamin.markod.objects.ShopList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    // child data in format of header title, child title
    private List<ShopList> shopLists;

    public ExpandableListAdapter(Context context, List<ShopList> shoplists) {
        this._context = context;
        this.shopLists = shoplists;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.shopLists.get(groupPosition).getProducts().get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = this.shopLists.get(groupPosition).getProducts().get(childPosition).getName();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.shoplist_list_item, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.shopLists.get(groupPosition).getProducts() != null)
            return this.shopLists.get(groupPosition).getProducts().size();
        else
            return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.shopLists.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        if (this.shopLists != null)
            return this.shopLists.size();
        else
            return 0;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        //String headerTitle = (String) getGroup(groupPosition);
        String headerTitle = this.shopLists.get(groupPosition).getName();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.shoplist_list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageButton searchBtn = (ImageButton) convertView.findViewById(R.id.id_search_shoplist);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(MainActivity.TAG, "SEARCH Clicked: " + groupPosition);
                if (_context instanceof ShopListsActivity) {
                    ((ShopListsActivity) _context).searchShopList(groupPosition);
                }
            }
        });

        ImageButton addToListBtn = (ImageButton) convertView.findViewById(R.id.id_add_to_shoplist);
        addToListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(MainActivity.TAG, "ADD Clicked: " + groupPosition);
                if (_context instanceof ShopListsActivity) {
                    ((ShopListsActivity) _context).addToShopList(groupPosition);
                }
            }
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
