package com.coderobot.receiptscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.coderobot.receiptscanner.model.Receipt;
import com.coderobot.receiptscanner.util.ReceiptUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScannerActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private static final String TAG = "ScannerActivity";
    public static final String KEY_RECEIPTS_JSON_STRING = "KEY_RECEIPTS_JSON_STRING";

    private int TASK_COUNT = 0;
    private ZBarScannerView mScannerView;
    private ImageView mImgSuccess;
    private ImageView mImageError;
    private boolean mFlash = false;

    private final ArrayList<Receipt> mReceipts = new ArrayList<>();

    final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scanner);

        mImgSuccess = (ImageView) findViewById(R.id.img_success);
        mImageError = (ImageView) findViewById(R.id.img_error);
        mScannerView = (ZBarScannerView) findViewById(R.id.scanner);
        mScannerView.setFormats(Collections.singletonList(BarcodeFormat.QRCODE));
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

        log("result = " + rawResult.getContents());

        new GetReceiptInfoTask().execute(rawResult.getContents());

        mScannerView.startCamera();
        mScannerView.setFlash(mFlash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_finish) {
            new AsyncTask<Void, Void, Void>() {

                ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    progressDialog = ProgressDialog.show(ScannerActivity.this, "Please wait", "Data processing, please wait.", true, false);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    while (TASK_COUNT != 0) ;
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progressDialog.dismiss();

                    ArrayList<String> strings = new ArrayList<>();

                    for (Receipt receipt : mReceipts) {
                        strings.add(receipt != null ? new Gson().toJson(receipt) : null);
                    }

                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(KEY_RECEIPTS_JSON_STRING, strings);
                    intent.putExtras(bundle);

                    setResult(RESULT_OK, intent);

                    finish();
                }
            }.execute();


        } else if (id == R.id.action_flash) {
            mFlash = !mScannerView.getFlash();
            mScannerView.setFlash(mFlash);

        }

        return super.onOptionsItemSelected(item);
    }

    private void showSuccessImg() {
        mImgSuccess.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImgSuccess.setVisibility(View.INVISIBLE);
            }
        }, 2000);
    }

    private void showErrorImg() {
        mImageError.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImageError.setVisibility(View.INVISIBLE);
            }
        }, 2000);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    public class GetReceiptInfoTask extends AsyncTask<String, Boolean, Receipt> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TASK_COUNT++;
        }

        @Override
        protected Receipt doInBackground(String... params) {

            String raw = params[0];
            Receipt receipt1 = null;
            Receipt receipt2 = null;

            try {
                receipt1 = ReceiptUtils.parseRaw(raw);
                receipt2 = ReceiptUtils.queryRecieptDetail(receipt1);

                synchronized (mReceipts) {
                    mReceipts.add(ReceiptUtils.mergeReceipts(receipt1, receipt2));
                }

            } catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {

                publishProgress(true);

                if (!receipt1.isComplete()) {
                    // todo show scan right part
                }
            } catch (ParseException e) {

                if (raw.startsWith("**")) {

                    if (mReceipts.isEmpty()) {
                        // todo show error 'please scan left part first'
                        publishProgress(false);
                    } else if (!mReceipts.get(mReceipts.size()).isComplete()) {
                        // todo parse right part and merge to one receipt
                        publishProgress(true);
                    } else {
                        // todo show error 'please scan left part first'
                        publishProgress(false);
                    }

                } else {
                    // todo show error 'not receipt format'

                    publishProgress(false);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Receipt receipt) {
            super.onPostExecute(receipt);
            TASK_COUNT--;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);

            if (values[0]) showSuccessImg();
            else showErrorImg();

        }
    }
}
