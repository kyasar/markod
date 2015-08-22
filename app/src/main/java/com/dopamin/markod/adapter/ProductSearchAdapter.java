package com.dopamin.markod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.dopamin.markod.objects.Product;
import com.dopamin.markod.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 23.08.2015.
 */
public class ProductSearchAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<Product> resultList = new ArrayList<Product>();

    public ProductSearchAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Product getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.product_autocomplete_row, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.tv_id_product_ac_name)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.tv_id_product_ac_name)).setText(getItem(position).getBarcode());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
