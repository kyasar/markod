package com.dopamin.markod.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class SimpleScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        //mScannerView.startCamera();
        Intent output = new Intent();
        output.putExtra("content", rawResult.getContents());
        output.putExtra("format", rawResult.getBarcodeFormat().getName());
        Log.v(MainActivity.TAG, "Barcode scan finished: " + rawResult.getContents() + " "
                + rawResult.getBarcodeFormat().getName());
        setResult(RESULT_OK, output);
        finish();
    }
}