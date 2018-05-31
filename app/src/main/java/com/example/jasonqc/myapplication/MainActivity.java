package com.example.jasonqc.myapplication;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jasonqc.myapplication.blueTooth.BluetoothFindListActivity;
import com.example.jasonqc.myapplication.blueTooth.BluetoothSocketUtil;
import com.example.jasonqc.myapplication.tcp.TcpClientSendMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    static final byte[] UWB101ID = {0x00, 0x00, 0x00, 0x65};
    static final byte[] UWB102ID = {0x00, 0x00, 0x00, 0x66};
    static final byte[] UWB103ID = {0x00, 0x00, 0x00, 0x67};
    static final byte[] UWB104ID = {0x00, 0x00, 0x00, 0x68};
    volatile boolean UWB101IDRangePermission = true;
    volatile boolean UWB102IDRangePermission = false;
    volatile boolean UWB103IDRangePermission = false;
    volatile boolean UWB104IDRangePermission = false;
    volatile int rangeTimes = 0;
    boolean trailCommt = false;
    boolean isRanging = false;


    /******************************************/
    final byte[] blueDataFrame = new byte[]{(byte) 0xA5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x023};
    /*****************************************/
    /*activity_main.xml id declare*/
    EditText sendTcpTxt;
    EditText ipEditText;
    EditText portEditText;
    Button blueRecvSwitch;
    Button oneblueSendButton;

    /**************************************/
    ExecutorService exec = Executors.newCachedThreadPool();
    Socket clientSocket = null;
    BluetoothSocketUtil bluetoothSocketUtil;


    /**************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendTcpTxt = findViewById(R.id.sendTcpTxt);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        blueRecvSwitch = findViewById(R.id.blueRecvSwitch);
        oneblueSendButton = findViewById(R.id.oneblueSendButton);
        bluetoothSocketUtil = (BluetoothSocketUtil) getApplication();
    }

    public void sendTcpData(View view) {
        if (clientSocket == null) {
            Toast.makeText(this, "请先建立连接", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(sendTcpTxt.getText())) {
                Toast.makeText(this, "请输入数据", Toast.LENGTH_SHORT).show();
            } else {
                exec.execute(new TcpClientSendMessage(sendTcpTxt.getText().toString().getBytes(), clientSocket));
            }
        }
    }

    public void connectToTcpHost(View view) {

        if (TextUtils.isEmpty(ipEditText.getText()) || TextUtils.isEmpty(portEditText.getText())) {
            Toast.makeText(this, "请输入ip 和 port", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("connecToTcpHost", ipEditText.getText().toString() + "," + portEditText.getText().toString());
            exec.execute(() -> {
                try {
                    clientSocket = new Socket(ipEditText.getText().toString(), Integer.parseInt(portEditText.getText().toString()));
                    Log.d("connectToTcpHost", String.valueOf(clientSocket.isConnected()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void disconnectToTcpHost(View view) {
        try {
            if (clientSocket == null)
                return;
            clientSocket.close();
            clientSocket = null;
            Log.d("disconnectToTcpHost", "disconnect");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotoBlueDeviceListActivity(View view) {
        Intent intent = new Intent(MainActivity.this, BluetoothFindListActivity.class);
        startActivity(intent);
    }

    public void closeBluetooth(View view) {

        bluetoothSocketUtil.closeBluetooth();
    }

    private void blueSendRangeRequest() {
        Log.d("blueSendRangeRequest","进入");
        if (!isRanging) {
            exec.execute(() -> {
                Log.d("blueSendRangeRequest","进入线程");
                isRanging = true;
                byte[] rangeRequireFrame = null;
                BluetoothSocket bluetoothSocket = bluetoothSocketUtil.getBluetoothSocket();
                OutputStream bluetoothSocketOutputStream;
                try {
                    if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                        bluetoothSocketOutputStream = bluetoothSocket.getOutputStream();
                        while (rangeTimes > 0 || trailCommt) {
                            if (UWB101IDRangePermission) {
                                UWB101IDRangePermission = false;
                                rangeRequireFrame = groupmessage(UWB101ID);
                                bluetoothSocketOutputStream.write(rangeRequireFrame);
                                Log.d("测距请求帧已发送", "101");
                            } else if (UWB102IDRangePermission) {
                                UWB102IDRangePermission = false;
                                rangeRequireFrame = groupmessage(UWB102ID);
                                bluetoothSocketOutputStream.write(rangeRequireFrame);
                                Log.d("测距请求帧已发送", "102");
                            } else if (UWB103IDRangePermission) {
                                UWB103IDRangePermission = false;
                                rangeRequireFrame = groupmessage(UWB103ID);
                                bluetoothSocketOutputStream.write(rangeRequireFrame);
                                Log.d("测距请求帧已发送", "103");
                            } else if (UWB104IDRangePermission) {
                                UWB104IDRangePermission = false;
                                rangeRequireFrame = groupmessage(UWB104ID);
                                bluetoothSocketOutputStream.write(rangeRequireFrame);
                                Log.d("测距请求帧已发送", "104");
                            }
                        }
                        isRanging = false;
                    } else {
                        Log.e("func oneblueSend", "蓝牙未连接");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            Toast.makeText(this, "请等待上一次测距命令结束", Toast.LENGTH_SHORT).show();
        }
    }

    public void oneblueSend(View view) {
        rangeTimes = 1;
        blueSendRangeRequest();
    }

    public void tenblueSend(View view) {
        rangeTimes = 10;
        blueSendRangeRequest();
    }

    public void trailblueSend(View view) {
        trailCommt = true;
        blueSendRangeRequest();
    }

    public void trailQuitblueSend(View view) {
        trailCommt = false;
    }

    public void blueRecv(View view) {
        if (bluetoothSocketUtil.getBluetoothSocket() == null) {
            Toast.makeText(this, "请先建立蓝牙连接", Toast.LENGTH_SHORT).show();
            return;
        }
        blueRecvSwitch.setEnabled(false);
        Log.d("蓝牙接收开启", "blueRecv");
        exec.execute(() -> {
            int countNum = 0;
            byte[] confirmAndDataFrame = new byte[100];
            BluetoothSocket bluetoothSocket = bluetoothSocketUtil.getBluetoothSocket();
            InputStream bluetoothSocketInputStream;
            byte[] blueRecvBytes = new byte[1024];
            int len = 0;
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                try {
                    bluetoothSocketInputStream = bluetoothSocket.getInputStream();
                    while ((len = bluetoothSocketInputStream.read(blueRecvBytes)) != -1) {
                        Log.d("len", String.valueOf(len));
                        for (int i = 0; i < len; i++) {
                            confirmAndDataFrame[countNum++] = blueRecvBytes[i];
                            if (countNum == 72 ) {
//                                一帧接收完成,并且帧头符合要求
                                //sendBlueDataFrameByTCP(confirmAndDataFrame); //将数据帧发送至服务器
                                int responseUWBNode = ((confirmAndDataFrame[22] & 0xff) << 24) + ((confirmAndDataFrame[23] & 0xff)
                                        << 16) + ((confirmAndDataFrame[24] & 0xff) << 8) + (confirmAndDataFrame[25] & 0xff);
                                int PRMerror = (confirmAndDataFrame[43] & 0xff) << 8 + confirmAndDataFrame[44] & 0xff;
                                if (confirmAndDataFrame[26] == 0x00) {
                                    Log.d("blueRecvBytes.length", String.valueOf(len));
                                    Log.d("PRMerror", String.valueOf(PRMerror));
                                    Log.d("responseUWBNode", String.valueOf(responseUWBNode));
                                    Log.d("蓝牙接收", "测距成功");
//                            测距状态为0x00 为测距成功
                                    switch (responseUWBNode) {
                                        case 101:
                                            Log.d("case101", "101接收完毕");
                                            blueDataFrame[2] = confirmAndDataFrame[30];
                                            blueDataFrame[3] = confirmAndDataFrame[31];
                                            blueDataFrame[4] = confirmAndDataFrame[32];
                                            blueDataFrame[5] = confirmAndDataFrame[33];
                                            UWB102IDRangePermission = true;
                                            break;
                                        case 102:
                                            Log.d("case102", "102接收完毕");
                                            blueDataFrame[6] = confirmAndDataFrame[30];
                                            blueDataFrame[7] = confirmAndDataFrame[31];
                                            blueDataFrame[8] = confirmAndDataFrame[32];
                                            blueDataFrame[9] = confirmAndDataFrame[33];
                                            UWB103IDRangePermission = true;
                                            break;
                                        case 103:
                                            Log.d("case103", "103接收完毕");
                                            blueDataFrame[10] = confirmAndDataFrame[30];
                                            blueDataFrame[11] = confirmAndDataFrame[31];
                                            blueDataFrame[12] = confirmAndDataFrame[32];
                                            blueDataFrame[13] = confirmAndDataFrame[33];
                                            UWB104IDRangePermission = true;
                                            break;
                                        case 104:
                                            Log.d("case104", "104接收完毕");
                                            blueDataFrame[14] = confirmAndDataFrame[30];
                                            blueDataFrame[15] = confirmAndDataFrame[31];
                                            blueDataFrame[16] = confirmAndDataFrame[32];
                                            blueDataFrame[17] = confirmAndDataFrame[33];
                                            rangeTimes--;
                                            sendBlueDataFrameByTCP(blueDataFrame); //将数据帧发送至服务器
                                            UWB101IDRangePermission = true;
                                            UWB102IDRangePermission = false;
                                            UWB103IDRangePermission = false;
                                            UWB104IDRangePermission = false;
                                            break;
                                        default:
                                            Log.d("func blueRecv switch", "No such UWBNode");
                                            break;
                                    }
                                } else {
                                    Log.e("测距失败重新测距", String.valueOf(responseUWBNode));
                                    switch (responseUWBNode) {
                                        case 101:
                                            UWB101IDRangePermission = true;
                                            break;
                                        case 102:
                                            UWB102IDRangePermission = true;
                                            break;
                                        case 103:
                                            UWB102IDRangePermission = true;
                                            break;
                                        case 104:
                                            UWB102IDRangePermission = true;
                                            break;
                                        default:
                                            UWB101IDRangePermission = true;
                                            UWB102IDRangePermission = false;
                                            UWB103IDRangePermission = false;
                                            UWB104IDRangePermission = false;
                                            break;
                                    }
                                }
                                countNum = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*targetUWBID：目标UWBID 封装测距请求帧*/
    public byte[] groupmessage(byte[] targetUWBID) {
        byte[] send_package = new byte[19];
        send_package[0] = (byte) 0xA5;
        send_package[1] = (byte) 0xA5;
        send_package[2] = (byte) 0x00;
        send_package[3] = (byte) 0x0D;
        send_package[4] = (byte) 0x00;
        send_package[5] = (byte) 0x03;
        send_package[6] = (byte) 0x00;
        send_package[7] = (byte) 0x64;
        send_package[8] = targetUWBID[0];
        send_package[9] = targetUWBID[1];
        send_package[10] = targetUWBID[2];
        send_package[11] = targetUWBID[3];
        send_package[12] = (byte) 0x00;
        send_package[13] = (byte) 0x00;
        send_package[14] = (byte) 0x00;
        send_package[15] = (byte) 0x00;
        send_package[16] = (byte) 0x00;
        byte[] toCRCbytes = new byte[13];// 待校验数据
        for (int i = 0; i < 13; i++) {
            toCRCbytes[i] = send_package[i + 4];
        }
        int CRCcodeInt = getCRC1021(toCRCbytes);// 获取int型校验码
        send_package[17] = Integer.valueOf((CRCcodeInt >> 8) & 0xff).byteValue();
        send_package[18] = Integer.valueOf(CRCcodeInt & 0xff).byteValue();
        return send_package;
    }

    /*获取int型校验码*/
    public static int getCRC1021(byte[] bytes) {
        int crc = 0x00;// initial value
        int polynomial = 0x1021;
        for (int index = 0; index < bytes.length; index++) {
            byte b = bytes[index];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);// 判断寄存器首位是否为1
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return crc;
    }

    private void sendBlueDataFrameByTCP(byte[] sendDataByTcp) {
        if (clientSocket == null) {
            Log.d("sendBlueDataFrameByTCP", "clientSocket == null");
        } else {
            exec.execute(new TcpClientSendMessage(sendDataByTcp, clientSocket));
        }
    }
}
