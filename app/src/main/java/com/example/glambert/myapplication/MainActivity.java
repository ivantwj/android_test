package com.example.glambert.myapplication;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    protected static final int DISCOVERY_REQUEST = 1;

    private BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public String toastText = "";
    private BluetoothDevice remoteDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button bluetoothButton = (Button)findViewById(R.id.button2);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //no idea why i cannot change the name to onClickBluetooth
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Connect Alert");
                builder.setMessage("Do you want to turn on your Bluetooth?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (!myBluetoothAdapter.isEnabled()) {
                            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOn, 0);
                            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
                        }
                        //finish();
                    }
                });
                builder.setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        /*BroadcastReceiver bluetoothState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        }*/

        Button connectButton = (Button)findViewById(R.id.button3);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
                String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
                IntentFilter filter = new IntentFilter(scanModeChanged);
                //registerReceiver(bluetoothState, filter);
                startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == DISCOVERY_REQUEST){
            Toast.makeText(MainActivity.this, "Discovery in progress", Toast.LENGTH_SHORT).show();
            //setupUI();
            findDevices();
        }
    }

    private void findDevices() {
        String lastUsedRemoteBTDevice = getLastUsedRemoteBTDevice();
        if (lastUsedRemoteBTDevice != null) {
            toastText = "Checking for known paired devices, name: " + lastUsedRemoteBTDevice;
            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
            //see if this device is in a list of currently visible(?), paired devices
            Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice pairedDevice : pairedDevices) {
                if (pairedDevice.getAddress().equals(lastUsedRemoteBTDevice)) {
                    toastText = "Found device: " + pairedDevice.getName() + "@" + lastUsedRemoteBTDevice;
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    remoteDevice = pairedDevice;
                }
            }
        }

        if (remoteDevice == null) {
            toastText = "Starting discovery for remote devices...";
            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
            //start discovery
            if (myBluetoothAdapter.startDiscovery()) {
                toastText = "Discovery thread started...Scanning for Devices";
                Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        }
    }

    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice;
            remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            toastText = "Discovered: " + remoteDeviceName;
            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
        }
    };

    private String getLastUsedRemoteBTDevice() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String result = prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);
        return result;
    }


    /*public void onButtonClick(View v){
        EditText e1 = (EditText)findViewById(R.id.editText);
        EditText e2 = (EditText)findViewById(R.id.editText2);
        TextView t1 = (TextView)findViewById(R.id.textView2);
        int num1 = Integer.parseInt(e1.getText().toString());
        int num2 = Integer.parseInt(e2.getText().toString());
        int sum = num1 + num2;
        t1.setText(Integer.toString(sum));
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
