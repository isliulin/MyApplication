package com.example.jasonqc.myapplication.tcp;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpClientSendMessage implements Runnable {
    Socket clientSocket;
    byte[] sendMessage;

    public TcpClientSendMessage(byte[] sendMessage, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.sendMessage = sendMessage;
    }

    @Override
    public void run() {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(clientSocket.getOutputStream());
            Log.d("tcpSendToHost",sendMessage.toString());
            dos.write(sendMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
