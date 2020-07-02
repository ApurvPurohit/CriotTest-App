package com.example.android.criottest;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QRCodeScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private SQLiteDatabase ourDatabase;

    String resultText = "";

    public String qrTempID;
    public String typeofSensor;

    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarbHandler = new Handler();
    AlertDialog alertDialog = null;

    SharedPreferences sharedpref;// 0 - for private mode
    public SharedPreferences.Editor cache;


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedpref = getApplicationContext().getSharedPreferences("CriotSensorCache", 0);
        cache = sharedpref.edit();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        scannerView = new ZXingScannerView(this); /* Initialize object */
        setContentView(scannerView); /* Set the ScannerView as a content of current activity */
    }


    @Override
    public void onResume() {
        super.onResume();
        /* Asking user to allow access of camera */
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            scannerView.setResultHandler(this); /* Set handler for ZXingScannerView */
            scannerView.startCamera(); /* Start camera */
        } else {
            ActivityCompat.requestPermissions(QRCodeScanActivity.this, new
                    String[]{Manifest.permission.CAMERA}, 1024);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera(); /* Stop camera */
    }

    @Override
    public void handleResult(Result scanResult) {


        ToneGenerator toneNotification = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100); /* Setting beep sound */

        toneNotification.startTone(ToneGenerator.TONE_PROP_BEEP, 150);


        resultText = scanResult.getText(); /* Retrieving text from QR Code */

            JSONObject obj = null;
        try {
            obj = new JSONObject(resultText);
            Toast.makeText(getApplicationContext(),resultText,Toast.LENGTH_LONG).show();
            qrTempID = obj.getString("QrID");
            typeofSensor = obj.getString("type");
            Intent i = new Intent(getApplicationContext(),BluetoothActivity.class);
            i.putExtra("key", qrTempID);
            i.putExtra("iv", "072967c949ea438db02718c389de6af5");
            i.putExtra("response", "success");
//                            i.putExtra("response", obj.getString("response") );
            startActivity(i);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        try {
//            obj = new JSONObject(resultText);
//            Toast.makeText(getApplicationContext(),resultText,Toast.LENGTH_LONG).show();
//            qrTempID = obj.getString("QrId");
//            typeofSensor = obj.getString("type");
//
//            boolean isScanned = false;
//
//            Map<String, ?> keys = sharedpref.getAll();
//            Log.d("TTTTTTTTKKKK", String.valueOf(keys));
//
//            for (Map.Entry<String, ?> entry : keys.entrySet()) {
//                Log.d("TTTTTTTTTTTT", entry.getKey());
//                Log.d("TTTTTTTTTTT", qrTempID);
//                boolean t = entry.getKey().equals(qrTempID);
//                Log.d("TTTTTTTTTTT", String.valueOf(t));
//
//                if (entry.getKey().equals(qrTempID)) {
//                    isScanned = true;
//                }
//            }
//
//
//            if (qrTempID != null && qrTempID.length() > 0 && !(isScanned)) {
//
//                progressBar = new ProgressDialog(QRCodeScanActivity.this);
//                progressBar.setCancelable(true);
//                progressBar.setMessage("Fetching Data from CRIOT Controller");
//                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                progressBar.setProgress(0);
//                progressBar.setMax(100);
//                progressBar.show();
//                progressBarbHandler.post(new Runnable() {
//                    public void run() {
//                        progressBar.setProgress(progressBarStatus);
//                    }
//                });
//                progressBarStatus++;
//
//                new Thread(new Runnable() {
//                    public void run() {
//
//                        try {
//
//
//
//                            URL url = new URL( "http://192.168.1.192:8080/users/sensors");
//
//                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                            connection = (HttpURLConnection) url.openConnection();
//                            ((HttpURLConnection) connection).setRequestMethod("POST");
//                            connection.setUseCaches(false);
//                            connection.setRequestMethod("POST");
//
//                            JSONObject jObject = new JSONObject();
//                            jObject.put("sensorid",qrTempID);
//                            Log.d("Zzzzzzzzzzzzz","ZZZZZZZ");
//
//                            connection.setDoOutput(true);
//                            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//                            out.write(jObject.toString());
//                            out.flush();
//
//                            int statusCode = connection.getResponseCode();
//
//                            Log.d("Zzzzzzzzzzzzz", String.valueOf(statusCode));
//
//                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                            StringBuilder sb = new StringBuilder();
//                            String line = null;
//
//                            // Read Server Response
//                            while ((line = in.readLine()) != null) {
//                                // Append server response in string
//                                sb.append(line + "\n");
//                                progressBarbHandler.post(new Runnable() {
//                                    public void run() {
//                                        progressBar.setProgress(progressBarStatus);
//                                    }
//                                });
//                                progressBarStatus++;
//                            }
//
//                            Log.d("ZZZZZZZZZZZZZZZZZZZ","DDDDDDDD");
//                            String response = sb.toString();
//                            out.close();
//                            in.close();
//                            JSONObject obj = new JSONObject(response);
//                            Log.d("Zzzzzzzzzzzzz",String.valueOf((obj)));
//
//                            if (obj.getString("response").equals("success")) {
//
//                                String ip_addr = getIntent().getStringExtra("Server IP");
//                                String s = getIntent().getStringExtra("SSID");
//                                String p = getIntent().getStringExtra("PW");
//                                String key = obj.getString("random_key");
//                                String iv = obj.getString("random_iv");
//                                Intent i = new Intent(getApplicationContext(),BluetoothActivity.class);
//                                i.putExtra("key", key);
//                                i.putExtra("iv", iv);
//                                i.putExtra("response", obj.getString("response") );
//                                startActivity(i);
//                                finish();
//
//                                cache.putString(qrTempID, obj.toString());
//                                cache.commit();
//                                /*
//                                 *
//                                 * In cache u have
//                                 *
//                                 * sensor name : {
//                                 *
//                                 *                   "id" :
//                                 *
//                                 *                   "ip":
//                                 *                   "type" :
//                                 *           } of that sensor
//                                 *
//                                 * */
//
//                                progressBar.dismiss();
//                                QRCodeScanActivity.this.runOnUiThread(new Runnable() {
//                                    public void run() {
//                                        //  Snackbar.make(findViewById(R.id.home), Html.fromHtml("<font color=\"#0DDEBF\">Added new Smart device ! Good to GO</font>"), Snackbar.LENGTH_LONG)
//                                        //         .setAction("Action", null).show();
//                                        showMessage(Html.fromHtml("<font color='#0DDEBF'>Added new smart device to your home</font>"));
//                                    }
//                                });
//
//                                SystemClock.sleep(2000);
//                            }
//                            else
//                            {
//                                Intent i = new Intent(getApplicationContext(),BluetoothActivity.class);
//                                i.putExtra("key", "072967c949ea438db02718c389de6af5fb1eb0e01d1adef69090e7392df6a8f0");
//                                i.putExtra("iv", "072967c949ea438db02718c389de6af5");
//                                i.putExtra("response", "success");
////                            i.putExtra("response", obj.getString("response") );
//                                startActivity(i);
//                                finish();
//
//                            }
//
//
//                        } catch (Exception e) {
//                            Intent i = new Intent(getApplicationContext(),BluetoothActivity.class);
//                            i.putExtra("key", "072967c949ea438db02718c389de6af5fb1eb0e01d1adef69090e7392df6a8f0");
//                            i.putExtra("iv", "072967c949ea438db02718c389de6af5");
//                            i.putExtra("response", "success");
////                            i.putExtra("response", obj.getString("response") );
//                            startActivity(i);
//                            finish();
//                            Log.d("Exception", e.toString());
//                        }
//                    }
//                }).start();
//
//            } else {
//
//                SystemClock.sleep(2000);
//                Log.d("KKKKKKKKKKKK", "hello");
//
//                QRCodeScanActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//
//                        //Snackbar.make(findViewById(android.R.id.content),
//                        // Html.fromHtml("<font color=\"#0DDEBF\">Added new Smart device ! Good to GO</font>"), Snackbar.LENGTH_LONG)
//                        //        .setAction("Action", null).show();
//                        showMessage(Html.fromHtml("<font color='#264675'>Smart device is already in</font>"));
//
//                    }
//                });
//
//                // Intent returnPage = new Intent(QrScanSensorConfiguration.this, HomeActivity.class);
//                // startActivity(returnPage);
//                // Toast.makeText(this, "Error: Might have scan other Qr/Bar code,plase scan valid one ..", Toast.LENGTH_LONG).show();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }


    private void showMessage(Spanned message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setCancelable(true);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        // Must call show() prior to fetching text view
        TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back.
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}

