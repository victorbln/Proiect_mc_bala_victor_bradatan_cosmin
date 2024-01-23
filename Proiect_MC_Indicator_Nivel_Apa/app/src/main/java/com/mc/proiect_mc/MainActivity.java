package com.mc.proiect_mc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView waterLevelTextView;
    private EditText macAddressEditText;
    private Button connectButton;
    private BluetoothAdapterClass bluetoothReceiver;
    private boolean isConnected = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waterLevelTextView = findViewById(R.id.receivingMessage);
        macAddressEditText = findViewById(R.id.macAddressInput);
        connectButton = findViewById(R.id.connectButton);

        // Handler to receive messages from BluetoothAdapterClass
        Handler messageHandler = new Handler(getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    String receivedMessage = (String) msg.obj;
                    waterLevelTextView.setText("Water Level: " + receivedMessage + " cm");
                }
            }
        };

        bluetoothReceiver = new BluetoothAdapterClass(this, messageHandler);

        connectButton.setOnClickListener(v -> {
            if (!isConnected) {
                bluetoothReceiver.enableBluetooth();
                String macAddress = macAddressEditText.getText().toString();
                if(macAddress.isEmpty()) {
                    Log.d(TAG, "No MAC address provided");
                    macAddress = "70:5F:A3:BD:65:54";
                }
                Log.d(TAG, "Attempting to connect to: " + macAddress);

                isConnected = bluetoothReceiver.connectToDevice(macAddress);

                if (isConnected) {
                    bluetoothReceiver.startListening();
                    connectButton.setText("Disconnect");
                    Log.d(TAG, "Connected to device and started listening");
                } else {
                    Log.d(TAG, "Failed to connect to device");
                }
            } else {
                bluetoothReceiver.disconnect();
                isConnected = false;
                connectButton.setText("Connect");
                Log.d(TAG, "Disconnected from device");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BluetoothAdapterClass.REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Bluetooth permission granted");
            } else {
                Log.d(TAG, "Bluetooth permission denied");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothAdapterClass.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth has been enabled, proceed with connection
                String macAddress = macAddressEditText.getText().toString();
                if (macAddress.isEmpty()) {
                    Log.d(TAG, "No MAC address provided");
                    macAddress = "98:D3:21:FC:8D:93"; // default MAC address MC_01 HC-05 MODULE
                }
                isConnected = bluetoothReceiver.connectToDevice(macAddress);
                if (isConnected) {
                    bluetoothReceiver.startListening();
                    connectButton.setText("Disconnect");
                } else {
                    Log.d(TAG, "Failed to connect to device");
                }
            } else {
                // User did not enable Bluetooth
                Log.d(TAG, "Bluetooth not enabled by user");
            }
        }
    }
}
