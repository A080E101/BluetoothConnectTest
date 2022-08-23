package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    InputStream btIn = null;
    OutputStream btOut = null;
    BluetoothSocket btSocket = null;
    BluetoothAdapter BA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView msg = (TextView) findViewById(R.id.msg);
        TextView rt = (TextView) findViewById(R.id.rt);
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            msg.setText("No Bluetooth support !");
        } else if (!BA.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 1);
        } else msg.setText("Bluetooth OK");

        BluetoothDevice device = BA.getRemoteDevice("98d3:11:fc36d3");
        msg.setText("Connecting to ... " + device);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        BA.cancelDiscovery();
        try {
            btSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
            btSocket.connect();
            btIn = btSocket.getInputStream();
            btOut = btSocket.getOutputStream();
            rt.setText("Connect Socketed ");
            (new rxtx()).start();//另外寫來通訊用的
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
            }
            msg.setText("Fail Socket " + e.toString());
        }
    }
        int len=-1;
        byte[] buffer = new byte[1024]; // buffer
        byte[] readBuffer = new byte[1024];
        int rBP = 0;
        String data;
        class rxtx extends Thread {
            public void run() {
                TextView rt = (TextView) findViewById(R.id.rt);
                byte delimiter = 13;
                try {
                    while (true) {
                        while ((len = btIn.read(buffer)) > -1) {
                            for (int i = 0; i < len; i++) {
                                byte b = buffer[i];
                                if (b == delimiter) {
                                    byte[] enBytes = new byte[rBP];
                                    System.arraycopy(readBuffer, 0, enBytes, 0, enBytes.length);
                                    data = new String(enBytes, "utf-8");
                                    data = data.trim();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            rt.setText(data);}
                                    });
                                    rBP = 0; }//delimiter
                                else readBuffer[rBP++] = b;
                            }//for
                        }//while len
                        Thread.sleep(100);
                    }//while true
                } catch (Exception e) {}
            }
        }
}