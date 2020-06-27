package com.example.android.criottest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView LatLong, BattPer, WifiName, WifiSSID;
    Button ble;
    WifiManager wifiManager;

    FusedLocationProviderClient fusedLocationProviderClient;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            String S = "Battery Percentage: "+String.valueOf(level)+"%";
            BattPer.setText(S);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        LatLong = findViewById(R.id.locationTextView);
        BattPer = findViewById(R.id.batteryTextView);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiName = findViewById(R.id.WifiNameTextView);
        WifiSSID = findViewById(R.id.SSIDTextView);
        ble = findViewById(R.id.button);
        ble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiInfo wifiInfo;
                wifiInfo = wifiManager.getConnectionInfo();
                Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                i.putExtra("IP_ADDR", String.valueOf(wifiInfo.getIpAddress()));
                startActivity(i);
            }
        });

        if(wifiManager != null)
        {   WifiInfo wifiInfo;
            wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            WifiSSID.setText("SSID: "+wifiInfo.getSSID());
            WifiName.setText("IP Address: "+String.valueOf(wifiInfo.getIpAddress()));
        }}

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getLocation();
        } else
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
            Intent i = getIntent();
            startActivity(i);
        }
    }

    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location!=null)
                {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        String s= " Latitude: "+(Math.round(addresses.get(0).getLatitude()*1000)/1000.0)+"\n"+"Longitude: "+(Math.round(addresses.get(0).getLongitude()*1000)/1000.0);
                        LatLong.setText(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }
    }

