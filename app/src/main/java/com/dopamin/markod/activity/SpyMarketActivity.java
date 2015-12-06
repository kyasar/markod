package com.dopamin.markod.activity;

import java.util.ArrayList;
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
import com.dopamin.markod.objects.TokenManager;
import com.dopamin.markod.objects.TokenResult;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.request.GsonRequest;
import com.dopamin.markod.scanner.SimpleScannerActivity;
import com.google.gson.Gson;
import de.hdodenhof.circleimageview.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SpyMarketActivity extends AppCompatActivity implements OnClickListener, TokenResult {
	
	private View scanBtn, sendBtn;
	private ListView products_lv;
	private List <Product> productList;
	private ListAdapter adapter;
	private Boolean test = false;
	protected AlertDialog.Builder builder;
    private ProgressDialog progressDialog;
	private RelativeLayout rl_spy_hint;
	private Toolbar toolbar;

	public static int POINTS_DIALOG_FRAGMENT_SUCC_CODE = 2;
	public static int PRICE_DIALOG_FRAGMENT_SUCC_CODE = 1;
	public static int PRICE_DIALOG_FRAGMENT_FAIL_CODE = 0;
	private int total = 0;
	private Product product = null;
	private Market market = null;
	private User user = null;
	private TokenManager tm;

	/* API socialLoginURL to retrieve info about a product with its unique BarCode number */
	String productURL = MainActivity.MDS_SERVER + "/mds/api/products/";
	String marketURL = MainActivity.MDS_SERVER + "/mds/api/market/" + "?token=";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spymarket);

		// Setting Toolbar
		// Set a Toolbar to replace the ActionBar.
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		scanBtn = findViewById(R.id.scan_button);
		sendBtn = findViewById(R.id.send_button);

		sendBtn.setVisibility(View.GONE);	// at first, nothing scanned to send
		products_lv = (ListView) findViewById(R.id.productList);
		registerForContextMenu(products_lv);
		rl_spy_hint = (RelativeLayout) findViewById(R.id.id_rl_spy_hint);

		scanBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		builder = new AlertDialog.Builder(this);

		productList = new ArrayList <Product> ();

        /* sharing product declarations loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		tm = new TokenManager(getApplicationContext());
		// Result will be returned to this Activity
		tm.delegateTokenResult = this;

		Bundle bundle = getIntent().getExtras();
		market = bundle.getParcelable("market");
		user = bundle.getParcelable("user");

		if (market != null && user != null) {
			getSupportActionBar().setTitle(market.getName());
			// Log.v(MainActivity.TAG, "MArket geo location: " + market.getGeoCoordinates());
		} else {
			Toast.makeText(this, "No Market or User defined !!", Toast.LENGTH_SHORT).show();
			Log.e(MainActivity.TAG, "Fatal Error. No Market or User defined !!");
			finish();
		}
	}
	
	private int isProductAlreadyAdded(Product p) {
		for (int i = 0; i < productList.size(); i++) {
			if (productList.get(i).getBarcode().matches(p.getBarcode())) {
				//Log.v(MainActivity.TAG, "The product " + p.getBarcode()
				//		+ " is already added. Just updating the price.");
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
		}
		rl_spy_hint.setVisibility(View.GONE);
		refreshScannedListView();
	}

    private void clearScannedList() {
        this.productList.clear();
		sendBtn.setVisibility(View.GONE);
		rl_spy_hint.setVisibility(View.VISIBLE);
		total = 0;
        refreshScannedListView();
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == MainActivity.BARCODE_REQUEST && resultCode == Activity.RESULT_OK) {
			Log.v(MainActivity.TAG, "Barcode scanning finished.");
			Bundle res = intent.getExtras();
			String barcode = res.getString("content");
			String format = res.getString("format");

			if (barcode != null) {
				progressDialog.setTitle(getResources().getString(R.string.spymarket_product_title));
				progressDialog.setMessage(getResources().getString(R.string.please_wait));
				progressDialog.show();
				// String scanFormat = scanningResult.getFormatName();
				String uniqueProductURL = productURL + barcode + "?api_key=" + MainActivity.MDS_API_KEY;
				// Log.v(MainActivity.TAG, "productURL: " + uniqueProductURL);

				@SuppressWarnings("unchecked")
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
						1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
						DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
				Volley.newRequestQueue(getApplication()).add(gsonRequest);
			}
			else {
				Toast.makeText(this, "No Barcode scanned ! Try again..", Toast.LENGTH_SHORT).show();
				Log.e(MainActivity.TAG, "No Barcode scanned !");
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

	private void sendJSONObjectRequest() {
		progressDialog.setTitle(getResources().getString(R.string.spymarket_sharing_title));
		progressDialog.setMessage(getResources().getString(R.string.please_wait));
		progressDialog.show();
		market.setProducts(this.productList);
		market.setUserID(user.get_id());

		Gson gson = new Gson();
		Log.v(MainActivity.TAG, "Market JSON: " + gson.toJson(market.createJSON_AssocProducts()));

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, marketURL + tm.getCurrentToken()
				+ "&api_key=" + MainActivity.MDS_API_KEY,
				gson.toJson(market.createJSON_AssocProducts()), new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {

				Log.i("volley", "response: " + response);
				String status = null;
				int earn = 0;

				try {
					status = response.get("status").toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (status != null) {
					if (status.equalsIgnoreCase("OK")) {
						//Toast.makeText(getApplicationContext(), "Product \"" + p.getName() +
						// "\" successfully sent! Thank you.", Toast.LENGTH_SHORT).show();
						try {
							JSONObject userJSON = response.getJSONObject("user");
							earn = userJSON.getInt("points") - user.getPoints();
						} catch (JSONException e) {
							e.printStackTrace();
							//snackIt(getResources().getString(R.string.str_msg_err_server));
						}
						user.incPoints(earn);
						//snackIt(getResources().getString(R.string.str_msg_product_declare_succ) + " " + earn);
						Log.v(MainActivity.TAG, "User has " + earn + " points.");

						clearScannedList();
						progressDialog.dismiss();
						showPointsDialog(response);
					}
					else if (status.equalsIgnoreCase("EXPIRED") || status.equalsIgnoreCase("NOTOKEN")) {
						Log.v(MainActivity.TAG, "Token expired or not provided");
						tm.getToken(user);
						Log.v(MainActivity.TAG, "New Token is being waited..");
					}
					else {
						Log.v(MainActivity.TAG, getResources().getString(R.string.str_msg_err_server));
						//snackIt(getResources().getString(R.string.str_msg_err_server));
						//setInputs(true);
						progressDialog.dismiss();
					}
				}
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
					if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
						Intent intent = new Intent(this, SimpleScannerActivity.class);
						startActivityForResult(intent, MainActivity.BARCODE_REQUEST);
					} else {
						Log.e(MainActivity.TAG, "Your device does not have a camera, sorry :(");
						// snackIt(getResources().getString(R.string.str_no_camera));
					}
				}
				break;

			case R.id.send_button:
				sendJSONObjectRequest();
				break;

			default:
				break;
		}
	}

	/* Price Fragment Dialog Select Button Listener */
	public void onUserSelectValue(int code, String value) {
		// TODO Auto-generated method stub
		if (code == PRICE_DIALOG_FRAGMENT_SUCC_CODE) {
			//Toast.makeText(this, "Entered price for " + product.getName() + " price: " + value, Toast.LENGTH_SHORT).show();
			product.setPrice(value);
			addProductToList(product);
		} else if (code == POINTS_DIALOG_FRAGMENT_SUCC_CODE) {
			updateUserPointsUI();
		} else if (code == PRICE_DIALOG_FRAGMENT_FAIL_CODE) {
			//Toast.makeText(this, "Product is discarded !!", Toast.LENGTH_SHORT).show();
			total--;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		if (v.getId() == R.id.productList) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.context_spy_product, menu);
			menu.setHeaderTitle(this.productList.get(info.position).getName());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Log.v(MainActivity.TAG, "Selected product: " + this.productList.get(info.position).getName());

		switch(item.getItemId()) {
			case R.id.id_menu_product_reprice:
				// Log.v(MainActivity.TAG, "Price will be updated..");
				showAlertDialog(this.productList.get(info.position));
				break;
			case R.id.id_menu_product_delete:
				// Log.v(MainActivity.TAG, "Price will be removed..");
				this.productList.remove(info.position);
				refreshScannedListView();
				break;
			default:
				break;
		}
		return super.onContextItemSelected(item);
	}

	public boolean saveUser(User user) {
		Gson gson = new Gson();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor edit = sp.edit();
		edit.putString("user", gson.toJson(user));
		return edit.commit();
	}

	public void updateUserPointsUI() {
		saveUser(this.user);
	}

	@Override
	public void tokenSuccess(String token) {
		Log.v(MainActivity.TAG, "Token SUCCESS: " + token);
		// retry request
		sendJSONObjectRequest();
	}

	@Override
	public void tokenExpired() {

	}

	@Override
	public void tokenFailed() {
		Log.e(MainActivity.TAG, "Token FAILED");
		progressDialog.dismiss();
	}
}