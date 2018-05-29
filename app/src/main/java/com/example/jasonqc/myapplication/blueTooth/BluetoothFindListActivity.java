package com.example.jasonqc.myapplication.blueTooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jasonqc.myapplication.MainActivity;
import com.example.jasonqc.myapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothFindListActivity extends Activity {
    public static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE = 1001;//启动蓝牙设备的请求码

    /***************************************************/
    Button backToMainActivityButton;
    Button searchBlueButtons;
    BluetoothAdapter mBluetoothAdapter;
    ListView pairedBlueText;
    ArrayAdapter pairedBlueAdapter;//适配器对象的定义
    ArrayList<String> pairedBlueDeviceNames = new ArrayList<>();
    ArrayList<BluetoothDevice> pairedBlueDevice = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.bluetooth_devicelist);
        super.onCreate(savedInstanceState);
        backToMainActivityButton = findViewById(R.id.backToMainActivityButton);
        pairedBlueText = findViewById(R.id.paired_devices);
        searchBlueButtons = findViewById(R.id.searchBlueButton);
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(blueScanReceiver,filter);
        IntentFilter filter2=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(blueScanReceiver,filter2);
        //创建适配器,使用系统布局
        pairedBlueAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedBlueDeviceNames);
       //给ListView设置适配器
        pairedBlueText.setAdapter(pairedBlueAdapter);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        pairedBlueText.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, "正在建立连接...", Toast.LENGTH_SHORT).show();
            connServer(pairedBlueDevice.get(position)); //与选中蓝牙设备建立连接
        });
    }

    private void connServer(BluetoothDevice bluetoothDevice) {
        BluetoothSocket bluetoothSocket = null;
        BluetoothSocketUtil bluetoothSocketUtil = (BluetoothSocketUtil) getApplication();
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();//连接Socket
            Log.d("BlueSocketIsConnected", String.valueOf(bluetoothSocket.isConnected()));
            if (bluetoothSocket.isConnected()){
                Toast.makeText(this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                bluetoothSocketUtil.setBluetoothSocket(bluetoothSocket);
                bluetoothSocketUtil.setmBluetoothAdapter(mBluetoothAdapter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //定义广播接收
    private BroadcastReceiver blueScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()==BluetoothDevice.BOND_BONDED)
                {    //显示已配对设备
                    pairedBlueDevice.add(device);
                    pairedBlueDeviceNames.add(TextUtils.isEmpty(device.getName()) ? "未命名" : device.getName());
                    pairedBlueAdapter.notifyDataSetChanged();
                }else if(device.getBondState()!=BluetoothDevice.BOND_BONDED)
                {
                  Log.e("新发现设备","不做任何处理");
                }
            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Toast.makeText(context, "蓝牙扫描完毕", Toast.LENGTH_SHORT).show();
            }
        }
    };
    public void scanBlueDevices(View view) {
        if (mBluetoothAdapter.isEnabled()) {
//            清空ArrayList
            pairedBlueDeviceNames.clear();
            pairedBlueAdapter.notifyDataSetChanged();
            mBluetoothAdapter.startDiscovery();
        } else {
            Toast.makeText(this, "请先开启蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    public void backToMainActivity(View view) {
        unregisterReceiver(blueScanReceiver);
        //创建一个意图
        Intent intent = new Intent(BluetoothFindListActivity.this, MainActivity.class);
        startActivity(intent);
        finish();//结束当前的Activity
        //如果没有上面的finish()，那么当跳转到MainActivity之后，SecondActivity只会onStop,不会ondestroy。即仍然还在栈中
        //需要注意的是，当它跳到MainActivity时，会去重新创建一个新的MainActivity，即执行MainActivity中的onCreate()方法;
    }

    public void openBlue(View view) {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE);
    }
}
