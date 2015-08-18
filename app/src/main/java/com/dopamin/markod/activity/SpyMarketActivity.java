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
import com.dopamin.markod.adapter.ProductListAdapter;
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.request.GsonRequest;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class SpyMarketActivity extends FragmentActivity implements OnClickListener {
	
	private View scanBtn, sendBtn;
	private TextView tv_points;
	private ListView products_lv;
	private List <HashMap<String, String>> productList;
	private ListAdapter adapter;
	private Boolean test = false;
	protected AlertDialog.Builder builder;
    private ProgressDialog progressDialog;
	private TextView tv_spymarket_name, tv_spymarket_info, tv_send;
	private CircleImageView profileView;
	
	public static int PRICE_DIALOG_FRAGMENT_SUCC_CODE = 1;
	public static int PRICE_DIALOG_FRAGMENT_FAIL_CODE = 0;
	private int total = 0;
	private Product product = null;
	private Market market = null;
	private User user = null;

	/* API url to retrieve info about a product with its unique BarCode number */
	String productURL = MainActivity.MDS_SERVER + "/mds/api/products/";
	String marketURL = MainActivity.MDS_SERVER + "/mds/api/market/" + "?token=" + MainActivity.MDS_TOKEN;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spymarket);
		
		Log.v(MarketSelectActivity.TAG, "onCreate Spy Market Activity.");
		
		scanBtn = findViewById(R.id.scan_button);
		sendBtn = findViewById(R.id.send_button);
		tv_send = (TextView) findViewById(R.id.tv_send_products);
		tv_points = (TextView) findViewById(R.id.id_tvPoints);

		tv_send.setVisibility(View.GONE);
		sendBtn.setVisibility(View.GONE);	// at first, nothing scanned to send
		products_lv = (ListView) findViewById(R.id.productList);
		tv_spymarket_name = (TextView) findViewById(R.id.spymarket_name);
		tv_spymarket_info = (TextView) findViewById(R.id.spymarket_info);
		profileView = (CircleImageView) findViewById(R.id.id_profile_image);
		
		scanBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		builder = new AlertDialog.Builder(this);
		
		if (savedInstanceState != null) {
			productList = (List<HashMap<String, String>>) savedInstanceState.getSerializable("productList");
			Log.v(MarketSelectActivity.TAG, "Restoring LIST.. size: " + productList.size());

			// list adapter
			adapter = new ProductListAdapter(this, productList);
			products_lv.setAdapter(adapter);
		} else {
			productList = new ArrayList <HashMap<String,String>> ();
		}

        /* sharing product declarations loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		Bundle bundle = getIntent().getExtras();
		market = (Market) bundle.getSerializable("market");
		user = (User) bundle.getSerializable("user");

		tv_points.setText(Integer.toString(user.getPoints()));
		tv_spymarket_name.setText(market.getName());

		byte[] b = Base64.decode(user.getEncodedProfilePhoto(), Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
		profileView.setImageBitmap(bitmap);
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
		adapter = new ProductListAdapter(this, productList);
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
			total++;
			sendBtn.setVisibility(View.VISIBLE);
			tv_send.setVisibility(View.VISIBLE);
		}
		tv_spymarket_info.setVisibility(View.GONE);
		refreshScannedListView();
	}

    private void clearScannedList() {
        this.productList.clear();
		sendBtn.setVisibility(View.GONE);
		tv_send.setVisibility(View.GONE);
		tv_spymarket_info.setVisibility(View.VISIBLE);
		total = 0;
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
					progressDialog.setTitle(getResources().getString(R.string.spymarket_product_title));
					progressDialog.setMessage(getResources().getString(R.string.please_wait));
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
				progressDialog.setTitle(getResources().getString(R.string.spymarket_sharing_title));
				progressDialog.setMessage(getResources().getString(R.string.please_wait));
                progressDialog.show();
				market.setProducts(this.productList);
                removeProductNames(); // Remove product names, they are not needed to keep in market

                Gson gson = new Gson();
				Log.v(MainActivity.TAG, "Market JSON: " + gson.toJson(market));

				JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, marketURL,
						gson.toJson(market), new Response.Listener<JSONObject>() {
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
				Log.v(MainActivity.TAG, "Sending " + total + " products to Market (" + market.getName() + ") ..");
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