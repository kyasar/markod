package com.dopamin.markod.adapter;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.objects.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kadir on 14.08.2015.
 */
public class MarketListAdapter extends ArrayAdapter<HashMap<String, String>> {
    private List<HashMap<String, String>> markets;

    public MarketListAdapter(Context context, List<HashMap<String, String>> markets) {
        super(context, 0, markets);
        this.markets = markets;
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

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Log.v(MainActivity.TAG, "Search string: " + constraint.toString() + " " + markets.size());
                    List<HashMap<String, String>> filtered_markets = new ArrayList<HashMap<String, String>>();
                    for (HashMap<String, String> p : markets) {

                        if (p.get("place_name").toUpperCase().startsWith(constraint.toString().toUpperCase()))
                            filtered_markets.add(p);
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = filtered_markets;
                    filterResults.count = filtered_markets.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.v(MainActivity.TAG, "publishResults");
                if (results != null && results.count > 0) {
                    Log.v(MainActivity.TAG, "publishResult OK.");
                    markets = (List<HashMap<String, String>>) results.values;
                    notifyDataSetChanged();
                } else {
                    Log.v(MainActivity.TAG, "publishResult Invalid.");
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
}
