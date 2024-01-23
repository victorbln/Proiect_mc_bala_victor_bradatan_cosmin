package com.conti.test2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_CONNECT = 100;
    private TextView statusTextView;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private final String NAME = "BluetoothServerTest";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Handler handler = new Handler();
    private final String macAddressProject = "70:5F:A3:BD:65:54";
    private final Random random = new Random();
    private TextView dataTextView;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = findViewById(R.id.statusTextView);
        dataTextView = findViewById(R.id.dataTextView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_BLUETOOTH_CONNECT);
        } else {
            startBluetoothServer();
        }
    }

    @SuppressLint("SetTextI18n")
    private void startBluetoothServer() {
        new Thread(() -> {
            while (true) { // Keep listening for new connections
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                    socket = serverSocket.accept(); // Blocking call, will wait until a connection is made
                    outputStream = socket.getOutputStream();

                    runOnUiThread(() -> statusTextView.setText("Connected"));

                    // Communication with the connected device
                    while (true) {
                        if (socket.isConnected()) {
                            float randomNumber = 2 + random.nextFloat() * 8; // Random number between 2 and 10
                            String message = randomNumber + "\n";
                            outputStream.write(message.getBytes());
                            runOnUiThread(() -> dataTextView.setText("Sent data from \"sensor\" to phone application: "+message));
                            Thread.sleep(2000); // Send data every 2 seconds
                        } else {
                            break; // Exit the loop if the socket is disconnected
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    closeSocket();
                    runOnUiThread(() -> statusTextView.setText("Disconnected"));
                } finally {
                    closeSocket(); // Close resources
                }
            }
        }).start();
    }


    private void closeSocket() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSocket(); // Ensure resources are freed when the app is closed
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                startBluetoothServer();
            } else {
                // Handle the case where permissions are not granted
            }
        }
    }

}
