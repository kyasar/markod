package com.dopamin.markod.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.PriceDialogFragment;
import com.dopamin.markod.R;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.request.GsonRequest;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

public class SpyMarketActivity extends FragmentActivity implements OnClickListener {
	
	private Button scanBtn, sendBtn;
	private ListView products_lv;
	private List <HashMap<String, String>> productList = new ArrayList <HashMap<String,String>> ();
	private ListAdapter adapter;
	private Boolean test = false;
	protected AlertDialog.Builder builder;
    private ProgressDialog progressDialog;
	
	public static int PRICE_DIALOG_FRAGMENT_SUCC_CODE = 1;
	public static int PRICE_DIALOG_FRAGMENT_FAIL_CODE = 0;
	private int total = 0;
	private Product product = null;

	/* API url to retrieve info about a product with its unique BarCode number */
	String productURL = MainActivity.MDS_SERVER + "/mds/api/products/";
	String marketURL = MainActivity.MDS_SERVER + "/mds/api/market/" + "?token=" + MainActivity.MDS_TOKEN;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spymarket);
		
		Log.v(MarketSelectActivity.TAG, "onCreate Spy Market Activity.");
		
		scanBtn = (Button)findViewById(R.id.scan_button);
		sendBtn = (Button)findViewById(R.id.send_button);
		products_lv = (ListView) findViewById(R.id.productList);
		
		scanBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		builder = new AlertDialog.Builder(this);
		
		if (savedInstanceState != null) {
			productList = (List<HashMap<String, String>>) savedInstanceState.getSerializable("productList");
			Log.v(MarketSelectActivity.TAG, "Restoring LIST.. size: " + productList.size());

			// list adapter
			adapter = new SimpleAdapter(SpyMarketActivity.this, productList,
					R.layout.product_list_item, new String[] { "name", "barcode", "price"}, new int[] {
	                        R.id.product_name, R.id.product_barcode, R.id.product_price });
	        
			products_lv.setAdapter(adapter);
		}

        /* sharing product declarations loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.markets_progress);
        progressDialog.setMessage(getResources().getString(R.string.spymarket_progress_message));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

    private void removeProductNames() {
        for (int i = 0; i < productList.size(); i++) {
            productList.get(i).remove("name");
        }
    }
	
	private int isProductAlreadyAdded(Product p) {
		for (int i = 0; i < productList.size(); i++) {
			if (productList.get(i).get("barcode").matches(p.getBarcode())) {
				Log.v(MarketSelectActivity.TAG, "The product " + p.getBarcode()
						+ " is already added. Just updating the price.");
				return i;
			}
		}
		return -1;
	}

    private void refreshScannedListView() {
        adapter = new SimpleAdapter(SpyMarketActivity.this, productList,
                R.layout.product_list_item, new String[] { "name", "barcode", "price"}, new int[] {
                R.id.product_name, R.id.product_barcode, R.id.product_price });

        // Adding data into listview
        products_lv.setAdapter(adapter);
    }
	
	private void addProductToList(Product p) {
		int index = isProductAlreadyAdded(p);
		
		if (index != -1) {
			productList.get(index).put("price", p.getPrice());
		} else {
			HashMap<String, String> hmProduct = new HashMap <String, String> ();
			hmProduct.put("name", p.getName());
			hmProduct.put("barcode", p.getBarcode());
			hmProduct.put("price", p.getPrice());
			
			productList.add(hmProduct);
		}
		refreshScannedListView();
	}

    private void clearScannedList() {
        this.productList.clear();
        refreshScannedListView();
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			// retrieve scan result
			IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanningResult != null) {
				// we have a result
				String scanContent = scanningResult.getContents();
				if (scanContent != null) {
                    progressDialog.show();
					Log.v(MarketSelectActivity.TAG, "scanContent: " + scanContent);
					// String scanFormat = scanningResult.getFormatName();
					String uniqueProductURL = productURL + scanContent + "?token=" + MainActivity.MDS_TOKEN;

					GsonRequest gsonRequest = new GsonRequest(Request.Method.GET, uniqueProductURL,
							Product.class, new Response.Listener<Product>() {
								@Override
								public void onResponse(Product p) {
									if (p != null) {
										System.out.println("Product name : " + p.getName());
										System.out.println("Barcode no   : " + p.getBarcode());

										// Set main product to use in other code parts
										product = p;
										showAlertDialog(product);
                                        progressDialog.dismiss();
									}
								}
							}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									if(error != null) {
										if (error instanceof ServerError) {
											Log.e(MainActivity.TAG, "Server Failure.");
											Toast.makeText(getApplicationContext(),
													"Server error !!", Toast.LENGTH_SHORT).show();
										} else if (error instanceof NetworkError) {
											Log.e(MainActivity.TAG, "Network Failure.");
											Toast.makeText(getApplicationContext(),
													"Network error !!", Toast.LENGTH_SHORT).show();
										} else {
											Log.e(MainActivity.TAG, "Unknown Failure.");
											Toast.makeText(getApplicationContext(),
													"Product is not found !!", Toast.LENGTH_SHORT).show();
										}
									}
                                    progressDialog.dismiss();
								}
					});

					/* Retry policy default timeout was 2500ms, now 3 * 5000 */
					gsonRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
							3, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
							DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
					Volley.newRequestQueue(getApplication()).add(gsonRequest);
				}
			}
		}
	}
	
	private void showAlertDialog(Product p) {
		if (p != null) { // Do I really need to check again?
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			PriceDialogFragment alertDialog = PriceDialogFragment.newInstance(p.getName());
			ft.add(alertDialog, "fragment_alert");
			// prevent data loss from screen rotates
			ft.commitAllowingStateLoss();
		} else {
			//TODO: Product not found warning, maybe a dialog
			Toast.makeText(this, "Product is not found !! We are Sorry :(", Toast.LENGTH_SHORT).show();
			Log.e(MainActivity.TAG, "Product (" + p.getBarcode()  + ") is not found !!");
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.scan_button:
				if (test) {
					product = new Product("Noname" + (++total), "0123456789", "");
					Log.v(MarketSelectActivity.TAG, "product created. name: " + product.getName() +
							", barcode: " + product.getBarcode());
					showAlertDialog(product);
				} else {
					// Scan Bar Code
					IntentIntegrator scanIntegrator = new IntentIntegrator(this);
					scanIntegrator.initiateScan();
				}
				break;
			case R.id.send_button:
                progressDialog.show();
				MainActivity.market.setProducts(this.productList);
                removeProductNames(); // Remove product names, they are not needed to keep in market

                Gson gson = new Gson();
				Log.v(MainActivity.TAG, "Market JSON: " + gson.toJson(MainActivity.market));

				JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, marketURL,
						gson.toJson(MainActivity.market), new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.i("volley", "response: " + response);
                        clearScannedList();
                        progressDialog.dismiss();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.i("volley", "error: " + error);
                        progressDialog.dismiss();
					}
				});
				Volley.newRequestQueue(getApplication()).add(jsObjRequest);

				break;
			default:
				break;
		}
	}
	
	public void onSaveInstanceState(Bundle savedState) {
		 super.onSaveInstanceState(savedState);
		 Log.v(MarketSelectActivity.TAG, "onSaveInstanceState Spy Market Activity.");
		 savedState.putSerializable("productList", (Serializable) productList);
	}

	/* Price Fragment Dialog Select Button Listener */
	public void onUserSelectValue(int code, String value) {
		// TODO Auto-generated method stub
		if (code == PRICE_DIALOG_FRAGMENT_SUCC_CODE) {
			Toast.makeText(this, "Entered price for " + product.getName() + " price: " + value, Toast.LENGTH_SHORT).show();
			product.setPrice(value);
			addProductToList(product);
		} else {
			Toast.makeText(this, "Product is discarded !!", Toast.LENGTH_SHORT).show();
			total--;
		}
	}
}