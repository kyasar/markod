package com.dopamin.markod.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.R;
import com.dopamin.markod.request.GsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 23.08.2015.
 */
public class ProductSearchAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<Product> products;

    public ProductSearchAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Product getItem(int index) {
        return products.get(index);
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
        ((TextView) convertView.findViewById(R.id.tv_id_product_ac_barcode)).setText(getItem(position).getBarcode());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Log.v(MainActivity.TAG, "Search string: " + constraint.toString());
                    findProducts(mContext, constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = products;
                    filterResults.count = products.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.v(MainActivity.TAG, "publishResults");
                if (results != null && results.count > 0) {
                    Log.v(MainActivity.TAG, "publishResult OK.");
                    products = (List<Product>) results.values;
                    notifyDataSetChanged();
                } else {
                    Log.v(MainActivity.TAG, "publishResult Invalid.");
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    /**
     * Returns a search result for the given book title.
     */
    private void findProducts(Context context, String search) {
        // GoogleBooksProtocol is a wrapper for the Google Books API
        //GoogleBooksProtocol protocol = new GoogleBooksProtocol(context, MAX_RESULTS);
        products = new ArrayList<Product>();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                MainActivity.MDS_SERVER + "/mds/api/products" + "?search=" + search,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MainActivity.TAG, response.toString());

                        try {
                            // TODO: Json respond check status?
                            // Parsing json array response
                            // loop through each json object
                            JSONArray jsonProducts = response.getJSONArray("product");

                            Log.v(MainActivity.TAG, "Product search array respond length: " + jsonProducts.length());
                            for (int i = 0; i < jsonProducts.length(); i++) {
                                JSONObject p = (JSONObject) jsonProducts.get(i);
                                String name = p.getString("name");
                                String barcode = p.getString("barcode");
                                Log.v(MainActivity.TAG, "Product after search name: " + name + " barcode: " + barcode);

                                products.add(new Product(name, barcode));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this.mContext).add(req);
    }
}
