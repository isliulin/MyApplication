package com.example.jasonqc.myapplication.tcp;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/*暂时没用到*/
public class TcpCreatSocket implements Runnable {
    String ip;
    String port;
    Socket clientSocket = null;

    public TcpCreatSocket(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientSocket = null;
    }

    public void tcpSendMessage(String sendMessage) {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeBytes(sendMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            clientSocket = new Socket(ip, Integer.parseInt(port));
            if (clientSocket.isConnected()) {
                Log.e("clientSocket", "connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
