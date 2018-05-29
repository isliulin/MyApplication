package com.example.jasonqc.myapplication.blueTooth;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

public class BluetoothSocketUtil extends Application {
    BluetoothSocket bluetoothSocket;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    public void closeBluetooth() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("closeBluetooth", "socket close 错误");
            }
        }
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.disable();
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }
}
