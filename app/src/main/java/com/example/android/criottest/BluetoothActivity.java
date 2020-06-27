package com.example.android.criottest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends Activity
{
    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    String ip_addr;
    EditText ssid, pw;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    private Button mScanBtn;
    private ProgressDialog mProgressDlg;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("MYDB",MODE_PRIVATE,null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ssid = findViewById(R.id.editText3);
        pw = findViewById(R.id.editText4);
        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        Button scnbtn = (Button)findViewById(R.id.button2);
        Button addbtn = (Button)findViewById(R.id.button3);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);
        myTextbox.setHint("Enter data to be sent here");
        //Add Button

        try {
            findBT();

        } catch (IOException e) {
            e.printStackTrace();
        }

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mydatabase.execSQL("CREATE TABLE IF NOT EXISTS WiFiDetails(ssid VARCHAR,pass VARCHAR);");
                mydatabase.execSQL("INSERT INTO WifiDetails VALUES('"+ssid.getText()+"','"+pw.getText()+"');");
                Toast.makeText(getApplicationContext(),"Table Created!",Toast.LENGTH_LONG).show();
            }
        });
        //Scan Button
        scnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(getApplicationContext(),QRCodeScanActivity.class);
                String s = String.valueOf(ssid.getText());
                String p = String.valueOf(pw.getText());
                x.putExtra("SSID", s);
                x.putExtra("PW", p);

                startActivity(x);
            }

        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });

    }

    void findBT() throws IOException {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        int flag=0;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("CRIOT-PI"))
                {
                    flag=1;
                    mmDevice = device;
                    openBT();
                    break;
                }
            }
        }
        if(flag==0) {
            myLabel.setText("Could'nt find CRIOT-PI");
            myLabel.setTextColor(Color.RED);
        }
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Connected to CRIOT-PI");
        myLabel.setTextColor(Color.parseColor("#228B22"));

    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException
    {
//        String tf = getIntent().getStringExtra("response");
        String tf = "success";
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        String k = getIntent().getStringExtra("key");
        String iv = getIntent().getStringExtra("iv");
        if(tf.equals("success"))
       { String msg = "CRIOT-PI:"+ssid.getText()+":"+pw.getText()+":"+String.valueOf(wifiInfo.getIpAddress())+":"+k+":"+iv;
           Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
//        mmOutputStream.write(msg.getBytes());
            myLabel.setText("Data Sent");
           myLabel.setTextColor(Color.parseColor("#228B22"));}
        else
        {
            Toast.makeText(getApplicationContext(),"Error in Response Code",Toast.LENGTH_LONG).show();
            myLabel.setText("Error! Data Not Sent");
        }
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Connect to CRIOT-PI");
        myLabel.setTextColor(Color.parseColor("#3949AB"));
    }
}






