package com.dopamin.markod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dopamin.markod.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kadir on 14.08.2015.
 */
public class ProductListAdapter extends ArrayAdapter<HashMap<String, String>> {
    public ProductListAdapter(Context context, List<HashMap<String, String>> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        // Check if an existing view is being reused, otherwise inflate the view
        HashMap<String, String> product = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_list_item , parent, false);
        }

        convertView.setBackgroundResource(R.drawable.listview_selector);

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.product_name);
        TextView tvBarcode = (TextView) convertView.findViewById(R.id.product_barcode);
        TextView tvPrice = (TextView) convertView.findViewById(R.id.product_price);

        // Populate the data into the template view using the data object
        tvName.setText(product.get("name"));
        tvBarcode.setText(product.get("barcode"));
        tvPrice.setText(product.get("price"));

        // Return the completed view to render on screen
        return convertView;
    }
}
