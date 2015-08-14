package com.dopamin.markod.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dopamin.markod.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kadir on 14.08.2015.
 */
public class MarketListAdapter extends ArrayAdapter<HashMap<String, String>> {
    public MarketListAdapter(Context context, List<HashMap<String, String>> markets) {
        super(context, 0, markets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        // Check if an existing view is being reused, otherwise inflate the view
        HashMap<String, String> market = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.market_list_item, parent, false);
        }

        convertView.setBackgroundResource(R.drawable.listview_selector);

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.place_name);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.place_address);

        // Populate the data into the template view using the data object
        tvName.setText(market.get("place_name"));
        tvAddress.setText(market.get("vicinity"));

        // Return the completed view to render on screen
        return convertView;
    }
}
