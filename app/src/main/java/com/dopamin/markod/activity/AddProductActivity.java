package com.dopamin.markod.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dd.processbutton.iml.ActionProcessButton;
import com.dopamin.markod.R;
import com.dopamin.markod.objects.Product;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.scanner.SimpleScannerActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private ImageButton btn_scanBarcode;
    private TextView txt_scannedCode, txt_photoTake;
    private EditText etxt_productDesc;
    private ActionProcessButton btn_sendProduct;
    private ImageView iv_photo, iv_take_photo, iv_barcode;
    private Bitmap bitmapPhoto;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private CoordinatorLayout snackbarCoordinatorLayout;

    private Product p;
    private User user;

    String productAddURL = MainActivity.MDS_SERVER + "/mds/api/products/" + "?token=" + MainActivity.MDS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Setting Toolbar
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        snackbarCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.snackbarCoordinatorLayout);

        btn_scanBarcode = (ImageButton) findViewById(R.id.id_btn_scanBarcode);
        btn_scanBarcode.setOnClickListener(this);

        btn_sendProduct = (ActionProcessButton) findViewById(R.id.id_btn_sendProduct);
        btn_sendProduct.setMode(ActionProcessButton.Mode.ENDLESS);
        btn_sendProduct.setOnClickListener(this);

        txt_scannedCode = (TextView) findViewById(R.id.txt_scannedCode);
        txt_photoTake =  (TextView) findViewById(R.id.id_txt_take_photo);
        etxt_productDesc = (EditText) findViewById(R.id.id_etxt_productDesc);
        etxt_productDesc.addTextChangedListener(this);

        iv_photo = (ImageView) findViewById(R.id.id_photo);
        iv_barcode = (ImageView) findViewById(R.id.id_img_barcode);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.id_btn_scanBarcode) {
            // Scan Bar Code
            //IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            //scanIntegrator.initiateScan();

            Intent intent = new Intent(this, SimpleScannerActivity.class);
            startActivityForResult(intent, MainActivity.BARCODE_REQUEST);
        }
        else if (view.getId() == R.id.id_take_photo) {
            Log.v(MainActivity.TAG, "Taking photo..");

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, MainActivity.CAMERA_REQUEST);
        }
        else if (view.getId() == R.id.id_btn_sendProduct) {
            //progressDialog.show();
            btn_sendProduct.setProgress(1);
            setInputs(false);
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
                        String status = null;
                        int earn = 0;

                        try {
                            status = response.get("status").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (status != null && status.equalsIgnoreCase("OK")) {
                            //Toast.makeText(getApplicationContext(), "Product \"" + p.getName() +
                            // "\" successfully sent! Thank you.", Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject userJSON = response.getJSONObject("user");
                                earn = userJSON.getInt("points") - user.getPoints();
                                user.incPoints(earn);
                                snackIt(getResources().getString(R.string.str_msg_product_declare_succ) + " " + earn);
                                Log.v(MainActivity.TAG, "User has " + earn + " points.");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                snackIt(getResources().getString(R.string.str_msg_err_server));
                            }
                        }
                        else {
                            snackIt(getResources().getString(R.string.str_msg_err_server));
                        }

                        clearInfos();
                        setInputs(true);
                        btn_sendProduct.setProgress(0);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btn_sendProduct.setProgress(0);
                        setInputs(true);
                        Log.e(MainActivity.TAG, "Volley: product add error.");
                        snackIt(getResources().getString(R.string.str_msg_err_server));
                    }
                });
                // Set timeout to 15 sec, and try only one time
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                        1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                Volley.newRequestQueue(getApplication()).add(jsObjRequest);
            } else {
                btn_sendProduct.setProgress(0);
                snackIt(getResources().getString(R.string.str_msg_err_no_user));
                Log.e(MainActivity.TAG, "No user found ! Cannot send..");
                Toast.makeText(getApplicationContext(),
                        "No User found !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == MainActivity.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
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
        else if (requestCode == MainActivity.BARCODE_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.v(MainActivity.TAG, "Barcode scanning finished.");
            Bundle res = intent.getExtras();
            String barcode = res.getString("content");
            String format = res.getString("format");

            if (barcode != null) {
                Log.v(MainActivity.TAG, "scanContent: " + barcode);
                txt_scannedCode.setText(barcode);
                etxt_productDesc.setVisibility(View.VISIBLE);
                etxt_productDesc.setFocusable(true);
                etxt_productDesc.requestFocus();

                try {
                    Bitmap bitmap = encodeAsBitmap(barcode, BarcodeFormat.CODE_128, 200, 50);
                    iv_barcode.setImageBitmap(bitmap);
                    iv_barcode.setVisibility(View.VISIBLE);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etxt_productDesc, InputMethodManager.SHOW_IMPLICIT);
            }
            else {
                //Toast.makeText(this, "No Barcode scanned ! Try again..", Toast.LENGTH_SHORT).show();
                snackIt(getResources().getString(R.string.str_msg_barcode_scan_fail));
                Log.e(MainActivity.TAG, "No Barcode scanned !");
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
        iv_barcode.setImageBitmap(null);
        iv_barcode.setVisibility(View.GONE);
        txt_photoTake.setVisibility(View.VISIBLE);
        txt_scannedCode.setText(getResources().getString(R.string.str_mock_barcode));

        etxt_productDesc.setText("");
        etxt_productDesc.clearFocus();
        etxt_productDesc.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etxt_productDesc.getWindowToken(), 0);
    }

    private void setInputs(boolean b) {
        etxt_productDesc.setEnabled(b);
        btn_sendProduct.setEnabled(b);
        iv_take_photo.setEnabled(b);
        iv_barcode.setEnabled(b);
    }

    public void snackIt(String msg) {
        Snackbar.make(snackbarCoordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }

    public boolean saveUser(User user) {
        Gson gson = new Gson();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("user", gson.toJson(user));
        Log.v(MainActivity.TAG, "User saved into Shared.");
        return edit.commit();
    }

    @Override
    protected void onPause() {
        saveUser(this.user);
        super.onPause();
    }

    /**************************************************************
     * getting from com.google.zxing.client.android.encode.QRCodeEncoder
     *
     * See the sites below
     * http://code.google.com/p/zxing/
     * http://code.google.com/p/zxing/source/browse/trunk/android/src/com/google/zxing/client/android/encode/EncodeActivity.java
     * http://code.google.com/p/zxing/source/browse/trunk/android/src/com/google/zxing/client/android/encode/QRCodeEncoder.java
     */

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}
