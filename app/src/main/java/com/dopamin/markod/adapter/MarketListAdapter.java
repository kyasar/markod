package com.dopamin.markod.adapter;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
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
public class MarketListAdapter extends BaseAdapter implements Filterable {
    private List<HashMap<String, String>> orig_markets;
    private List<HashMap<String, String>> markets;
    private List<HashMap<String, String>> filtered_markets;
    private Context mContext;

    public MarketListAdapter(Context context, List<HashMap<String, String>> markets) {
        this.mContext = context;
        this.orig_markets = markets;
        this.markets = markets;
    }

    @Override
    public int getCount() {
        return markets.size();
    }

    @Override
    public HashMap<String, String> getItem(int index) {
        return markets.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        // Check if an existing view is being reused, otherwise inflate the view
        HashMap<String, String> market = (HashMap<String, String>) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.market_list_item, parent, false);
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
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                String filterString = constraint.toString().toLowerCase();
                Log.v(MainActivity.TAG, "Filter string: " + filterString);

                if (constraint != null) {
                    filtered_markets = new ArrayList<HashMap<String, String>>();
                    for (HashMap<String, String> m : orig_markets) {
                        if (m.get("place_name").toLowerCase().startsWith(filterString))
                            filtered_markets.add(m);
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
                    filtered_markets = (List<HashMap<String, String>>) results.values;
                    markets = filtered_markets;
                    Log.v(MainActivity.TAG, "publishResult OK. " + filtered_markets.size());
                    notifyDataSetChanged();
                } else {
                    Log.v(MainActivity.TAG, "publishResult Invalid.");
                    notifyDataSetInvalidated();
                }
            }};
    }
}
