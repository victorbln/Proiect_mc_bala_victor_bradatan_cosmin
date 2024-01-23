package com.mc.proiect_mc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothAdapterClass {

    public static final int REQUEST_ENABLE_BT = 1;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private boolean isConnected = false;
    private final Context context;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Handler handler;

    public BluetoothAdapterClass(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startListening() {
        new Thread(() -> {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (isConnected) {
                try {
                    String data = bufferedReader.readLine();
                    if (data != null) {
                        handler.obtainMessage(1, data).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e("BluetoothAdapterClass", "Error reading from Bluetooth Socket", e);
                    isConnected = false;
                    handler.post(() -> {
                        // Update UI or inform user about disconnection
                    });
                    closeResources();
                    break;
                }
            }
        }).start();
    }

    private void closeResources() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e("BluetoothAdapterClass", "Error closing Bluetooth resources", e);
        }
    }

    public void enableBluetooth() {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothAdapterClass", "Bluetooth not supported");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean connectToDevice(String macAddress) {
        Log.d("BluetoothConnect", "Attempting to connect to device: " + macAddress);

        if (!bluetoothAdapter.isEnabled()) {
            return false;
        }

        try {
            if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                return false;
            }
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                    return false;
                }
            }

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            isConnected = true;
            return true;
        } catch (IOException e) {
            Log.e("BluetoothConnect", "Unable to connect to device", e);
            isConnected = false;
            return false;
        }
    }

    public void disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                isConnected = false;
            } catch (IOException e) {
                Log.e("BluetoothAdapterClass", "Error closing Bluetooth Socket", e);
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
