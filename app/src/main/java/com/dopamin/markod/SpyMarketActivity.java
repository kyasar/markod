package com.dopamin.markod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.request.GsonRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.AlertDialog;
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

public class SpyMarketActivity extends FragmentActivity implements OnClickListener {
	
	private Button scanBtn;
	private ListView products_lv;
	private List <HashMap<String, String>> productList = new ArrayList <HashMap<String,String>> ();
	private ListAdapter adapter;
	private Boolean test = false;
	protected AlertDialog.Builder builder;
	
	public static int PRICE_DIALOG_FRAGMENT_SUCC_CODE = 1;
	public static int PRICE_DIALOG_FRAGMENT_FAIL_CODE = 0;
	private int total = 0;
	private Product product = null;

	/* API url to retrieve info about a product with its unique BarCode number */
	String productURL = MainActivity.MDS_SERVER + "/mds/api/products/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spymarket);
		
		Log.v(MarketSelectActivity.TAG, "onCreate Spy Market Activity.");
		
		scanBtn = (Button)findViewById(R.id.scan_button);
		products_lv = (ListView) findViewById(R.id.productList);
		
		scanBtn.setOnClickListener(this);
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
		adapter = new SimpleAdapter(SpyMarketActivity.this, productList, 
        		R.layout.product_list_item, new String[] { "name", "barcode", "price"}, new int[] {
                        R.id.product_name, R.id.product_barcode, R.id.product_price });
        
        // Adding data into listview
        products_lv.setAdapter(adapter);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			// retrieve scan result
			IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanningResult != null) {
				// we have a result
				String scanContent = scanningResult.getContents();
				if (scanContent != null) { 
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
									}
								}
							}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError volleyError) {
									if(volleyError != null) {
										Log.e(MainActivity.TAG, volleyError.getMessage());
										Toast.makeText(getApplicationContext(),
												"Product is not found !!", Toast.LENGTH_SHORT).show();
									}
								}
					});

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
		if (v.getId() == R.id.scan_button) {
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