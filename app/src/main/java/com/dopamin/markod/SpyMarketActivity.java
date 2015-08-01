package com.dopamin.markod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dopamin.markod.objects.Product;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.AlertDialog;
import android.os.AsyncTask;
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
	
	private Button scanBtn;
	private ListView products_lv;
	private List <HashMap<String, String>> productList = new ArrayList <HashMap<String,String>> ();
	private ListAdapter adapter;
	private Boolean test = false;
	protected AlertDialog.Builder builder;
	
	public static int PRICE_DIALOG_FRAGMENT_SUCC_CODE = 1;
	public static int PRICE_DIALOG_FRAGMENT_FAIL_CODE = 0;
	private int total = 0;
	private Product product;
	
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
			//product = (Product) savedInstanceState.getParcelable("product");
			//Log.v(MarketSelectActivity.TAG, "Restoring product: " + product.getName());
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
				Log.v(MarketSelectActivity.TAG, "The product " + p.getBarcode() + " is already added. Just updating the price.");
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
	
					//product = new Product("Product_" + (++total), scanContent);
					getProductInfo(scanContent, MainActivity.MDS_TOKEN);
					//Log.v(MarketSelectActivity.TAG, "product created. name: " + product.getName() + "  barcode: " + product.getBarcode());
					showAlertDialog("Product_" + total);
				}
			}
		}
	}
	
	private void showAlertDialog(String title) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			//FragmentManager fm = getSupportFragmentManager();
			PriceDialogFragment alertDialog = PriceDialogFragment.newInstance(title);
			
			ft.add(alertDialog, "fragment_alert");
			ft.commitAllowingStateLoss();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.scan_button) {
			if (test) {
				product = new Product("Noname" + (++total), "0123456789", "");
				Log.v(MarketSelectActivity.TAG, "product created. name: " + product.getName() + "  barcode: " + product.getBarcode());
				showAlertDialog("Product_" + total);
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

	public void onUserSelectValue(int code, String value) {
		// TODO Auto-generated method stub
		if (code == PRICE_DIALOG_FRAGMENT_SUCC_CODE) {
			Toast.makeText(this, "Entered price for product_" + total + " : " + value, Toast.LENGTH_SHORT).show();
			product.setPrice(value);
			addProductToList(product);
		} else {
			Toast.makeText(this, "Product is discarded !!", Toast.LENGTH_SHORT).show();
			total--;
		}
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;

		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb  = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			data = sb.toString();

			//Log.v(TAG, "RES: " + data);
			br.close();

		} catch (Exception e) {
			Log.v(MainActivity.TAG, "Exception while downloading url");
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}

		return data;
	}

	public void getProductInfo(String barcode, String token) {
		StringBuilder sb = new StringBuilder(MainActivity.MDS_SERVER + "/mds/api/products/");
		sb.append(barcode + "/");
		sb.append("?token=" + token);

		Log.v(MainActivity.TAG, "Product info query: " + sb.toString());

		// Creating a new non-ui thread task to download Google place json data
		ProductInfoTask task = new ProductInfoTask();

		// Invokes the "doInBackground()" method of the class PlaceTask
		task.execute(sb.toString());
	}

	/** A class, to download Google Places */
	private class ProductInfoTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		// Does not affect main activity
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e){
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		// This method is executed in Main Activity
		@Override
		protected void onPostExecute(String result) {
			ParserTask parserTask = new ParserTask();

			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, Product> {

		JSONObject jObject;
		Product product = null;

		// Invoked by execute() method of this object
		@Override
		protected Product doInBackground(String... jsonData) {

			try {
				jObject = new JSONObject(jsonData[0]);
				product = new Product(jObject.getString("name"), jObject.getString("barcodeNumber"));
				Log.v(MainActivity.TAG, "Product Info: " + product.getName() + " " + product.getBarcode());
				/** Getting the parsed data as a List construct */

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return product;
		}
	}
}