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
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kadir on 14.08.2015.
 */
public class MarketListAdapter extends BaseAdapter {
    public enum LIST_TYPE {
        MARKET_SELECT,
        MARKET_SCAN,
    }
    private List<Market> markets;
    private Context mContext;
    private LIST_TYPE list_type;

    public MarketListAdapter(Context context, List<Market> markets, LIST_TYPE list_type) {
        this.mContext = context;
        this.markets = markets;
        this.list_type = list_type;
    }

    public static class ViewHolder {
        public TextView tvName;
        public TextView tvAddress;
        public TextView tvPrice;
        public TextView tvMissing;
        public int position;
    }

    public void setData(List<Market> markets) {
        this.markets = markets;
    }

    @Override
    public int getCount() {
        return markets.size();
    }

    @Override
    public Market getItem(int index) {
        return markets.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 500;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        // Check if an existing view is being reused, otherwise inflate the view
        final Market market = getItem(position);
        ViewHolder viewHolder = null;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            switch (this.list_type) {
                case MARKET_SELECT:
                    convertView = LayoutInflater.from(this.mContext).inflate(R.layout.market_list_item, parent, false);
                    break;
                case MARKET_SCAN:
                    convertView = LayoutInflater.from(this.mContext).inflate(R.layout.market_results_list_item, parent, false);
                    viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.total_price);
                    viewHolder.tvMissing = (TextView) convertView.findViewById(R.id.missing);
                    viewHolder.position = position;
                    break;
            }
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.place_name);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.place_address);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundResource(R.drawable.listview_selector);


        if (list_type == LIST_TYPE.MARKET_SCAN) {
            viewHolder.tvPrice.setText(market.getTotalPrice());

            if (markets.get(viewHolder.position).getMissing() > 0) {
                viewHolder.tvMissing.setText(markets.get(viewHolder.position).getMissing() + " "
                        + this.mContext.getResources().getString(R.string.str_missing_products));
                viewHolder.tvMissing.setVisibility(View.VISIBLE);
            }
        }

        // Populate the data into the template view using the data object
        viewHolder.tvName.setText(market.getName());
        viewHolder.tvAddress.setText(market.getVicinity());

        // Return the completed view to render on screen
        return convertView;
    }
}
