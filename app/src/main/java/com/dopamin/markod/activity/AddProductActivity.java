package com.dopamin.markod.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.R;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.request.GsonRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class AddProductActivity extends Activity implements View.OnClickListener, TextWatcher {

    private ImageButton btn_scanBarcode;
    private TextView txt_scannedCode, txt_photoTake;
    private EditText etxt_productDesc;
    private Button btn_sendProduct, btn_back;
    private ImageView iv_photo, iv_take_photo;
    private Bitmap bitmapPhoto;
    private ProgressDialog progressDialog;

    private Product p;
    private User user;

    private static final int CAMERA_REQUEST = 1888;

    String productAddURL = MainActivity.MDS_SERVER + "/mds/api/products/" + "?token=" + MainActivity.MDS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        btn_scanBarcode = (ImageButton) findViewById(R.id.id_btn_scanBarcode);
        btn_scanBarcode.setOnClickListener(this);

        btn_sendProduct = (Button) findViewById(R.id.id_btn_sendProduct);
        btn_sendProduct.setOnClickListener(this);

        btn_back = (Button) findViewById(R.id.id_btn_back);
        btn_back.setOnClickListener(this);

        txt_scannedCode = (TextView) findViewById(R.id.txt_scannedCode);
        txt_photoTake =  (TextView) findViewById(R.id.id_txt_take_photo);
        etxt_productDesc = (EditText) findViewById(R.id.id_etxt_productDesc);
        etxt_productDesc.addTextChangedListener(this);

        iv_photo = (ImageView) findViewById(R.id.id_photo);
        iv_take_photo = (ImageView) findViewById(R.id.id_take_photo);
        iv_take_photo.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        user = bundle.getParcelable("user");

        /* sending product declaration loading progress */
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getResources().getString(R.string.str_send_product_please_wait));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_scanBarcode) {
            // Scan Bar Code
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        else if (view.getId() == R.id.id_btn_back) {
            Log.v(MainActivity.TAG, "Return back to Main menu..");
            finish();
        }
        else if (view.getId() == R.id.id_take_photo) {
            Log.v(MainActivity.TAG, "Taking photo..");

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        else if (view.getId() == R.id.id_btn_sendProduct) {
            progressDialog.show();
            Log.v(MainActivity.TAG, "Sending Product..");

            p = new Product(etxt_productDesc.getText().toString().trim(), txt_scannedCode.getText().toString());
            if (bitmapPhoto != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmapPhoto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                p.setEncodedPhoto(Base64.encodeToString(imageBytes, Base64.DEFAULT));
            }

            if (user != null) {
                p.setUserID(user.get_id());
                Gson gson = new Gson();
                Log.v(MainActivity.TAG, "Product: " + gson.toJson(p).toString());

                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, productAddURL,
                        gson.toJson(p), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("volley", "response: " + response);
                        clearInfos();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Product \"" + p.getName() +  "\" successfully sent! Thank you.", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e(MainActivity.TAG, "Volley: product add error.");
                        Toast.makeText(getApplicationContext(),
                                "Server error ! Try again later..", Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.v(MainActivity.TAG, "Sending " + total + " products to Market (" + market.getName() + ") ..");
                Volley.newRequestQueue(getApplication()).add(jsObjRequest);
            } else {
                progressDialog.dismiss();
                Log.e(MainActivity.TAG, "No user found ! Cannot send..");
                Toast.makeText(getApplicationContext(),
                        "No User found !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            // retrieve scan result
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanningResult != null) {
                // we have a result
                String scanContent = scanningResult.getContents();
                if (scanContent != null) {
                    Log.v(MainActivity.TAG, "scanContent: " + scanContent);
                    txt_scannedCode.setText(scanContent);
                    etxt_productDesc.setVisibility(View.VISIBLE);
                    etxt_productDesc.setFocusable(true);
                    etxt_productDesc.requestFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etxt_productDesc, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.v(MainActivity.TAG, "Camera finished.");
            if (intent.getExtras().get("data") != null) {
                if (bitmapPhoto != null)
                    bitmapPhoto.recycle();
                bitmapPhoto = (Bitmap) intent.getExtras().get("data");

                Log.v(MainActivity.TAG, "H: " + bitmapPhoto.getHeight() + ", W: " + bitmapPhoto.getWidth());
                //iv_photo.setMaxHeight(bitmapPhoto.getHeight());
                //iv_photo.setMaxWidth(bitmapPhoto.getWidth());
                txt_photoTake.setVisibility(View.GONE);
                iv_photo.setImageBitmap(bitmapPhoto);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.toString().trim().length() > 6) {
            btn_sendProduct.setVisibility(View.VISIBLE);
        } else {
            btn_sendProduct.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void clearInfos() {
        if (bitmapPhoto != null) {
            bitmapPhoto.recycle();
            bitmapPhoto = null;
        }
        iv_photo.setImageBitmap(null);
        txt_photoTake.setVisibility(View.VISIBLE);
        txt_scannedCode.setText(getResources().getString(R.string.str_mock_barcode));

        etxt_productDesc.setText("");
        etxt_productDesc.clearFocus();
        etxt_productDesc.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etxt_productDesc.getWindowToken(), 0);
    }
}
