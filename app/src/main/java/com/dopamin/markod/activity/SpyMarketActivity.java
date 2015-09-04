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
import com.dopamin.markod.dialog.*;
import com.dopamin.markod.R;
import com.dopamin.markod.adapter.*;
import com.dopamin.markod.objects.Market;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.request.GsonRequest;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import de.hdodenhof.circleimageview.*;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
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

import org.json.JSONException;
import org.json.JSONObject;

public class SpyMarketActivity extends FragmentActivity implements OnClickListener {
	
	private View scanBtn, sendBtn;
	private TextView tv_points;
	private ListView products_lv;
	private List <Product> productList;
	private ListAdapter adapter;
	private Boolean test = false;
	protected AlertDialog.Builder builder;
    private ProgressDialog progressDialog;
	private TextView tv_spymarket_name, tv_spymarket_info, tv_send;
	private CircleImageView profileView;

	public static int POINTS_DIALOG_FRAGMENT_SUCC_CODE = 2;
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
		
		Log.v(MainActivity.TAG, "onCreate Spy Market Activity.");
		
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

		/* Product List TODO: Convert Hashmap to Product object */
		productList = new ArrayList <Product> ();

        /* sharing product declarations loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		Bundle bundle = getIntent().getExtras();
		market = bundle.getParcelable("market");
		user = bundle.getParcelable("user");

		tv_points.setText(Integer.toString(user.getPoints()));
		tv_spymarket_name.setText(market.getName());

		byte[] b = Base64.decode(user.getEncodedProfilePhoto(), Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
		profileView.setImageBitmap(bitmap);
	}
	
	private int isProductAlreadyAdded(Product p) {
		for (int i = 0; i < productList.size(); i++) {
			if (productList.get(i).getBarcode().matches(p.getBarcode())) {
				Log.v(MainActivity.TAG, "The product " + p.getBarcode()
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
			productList.get(index).setPrice(p.getPrice());
		} else {
			productList.add(p);
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
					Log.v(MainActivity.TAG, "scanContent: " + scanContent);
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

	private void showPointsDialog(JSONObject response) {
		int total = 0, new_products = 0, update_products = 0;
		boolean new_market = false;

		try {
			if (response.get("status").toString().equals("OK")) {
				// Only 1 market can be new
				if (Integer.parseInt(response.get("new_market").toString()) > 0) {
					new_market = true;
					total += Integer.parseInt(response.get("coeff_nm").toString());
				}

				// new products declared in this market
				new_products = Integer.parseInt(response.get("new_products").toString());
				total += new_products *	Integer.parseInt(response.get("coeff_np").toString());

				// products already declared in this market
				update_products = Integer.parseInt(response.get("products").toString());
				total +=  update_products *	Integer.parseInt(response.get("coeff_p").toString());

				Log.v(MainActivity.TAG, "You earned " + total + " points.");
				user.incPoints(total);
			} else {
				Log.v(MainActivity.TAG, "Unexpected error on server-side.");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		PointsDialogFragment alertDialog = PointsDialogFragment.newInstance(getResources().getString(R.string.title_points_earned),
				total, new_market, new_products, update_products);
		ft.add(alertDialog, "fragment_alert");
		ft.commitAllowingStateLoss();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.scan_button:
				if (test) {
					product = new Product("Noname" + (++total), "0123456789", "");
					Log.v(MainActivity.TAG, "product created. name: " + product.getName() +
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
				market.setUserID(user.get_id());

                Gson gson = new Gson();
				Log.v(MainActivity.TAG, "Market JSON: " + gson.toJson(market.createJSON_AssocProducts()));

				JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, marketURL,
						gson.toJson(market), new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.i("volley", "response: " + response);
                        clearScannedList();
                        progressDialog.dismiss();
						showPointsDialog(response);
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

	/* Price Fragment Dialog Select Button Listener */
	public void onUserSelectValue(int code, String value) {
		// TODO Auto-generated method stub
		if (code == PRICE_DIALOG_FRAGMENT_SUCC_CODE) {
			Toast.makeText(this, "Entered price for " + product.getName() + " price: " + value, Toast.LENGTH_SHORT).show();
			product.setPrice(value);
			addProductToList(product);
		} else if (code == POINTS_DIALOG_FRAGMENT_SUCC_CODE) {
			updateUserPointsUI();
		} else if (code == PRICE_DIALOG_FRAGMENT_FAIL_CODE) {
			Toast.makeText(this, "Product is discarded !!", Toast.LENGTH_SHORT).show();
			total--;
		}
	}

	public boolean saveUser(User user) {
		Gson gson = new Gson();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor edit = sp.edit();
		edit.putString("user", gson.toJson(user));
		return edit.commit();
	}

	public void updateUserPointsUI() {
		tv_points.setText(Integer.toString(user.getPoints()));
		saveUser(this.user);
	}
}