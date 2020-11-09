package com.anjanik012.suto.Backend;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

public class SutoConnection implements Runnable {
    private static final String TAG = "SutoConnection";

    public enum States {
        INACTIVE, ACTIVE, HELLO, TCP_CONNECTED
    }

    public interface TCPConnectionEstablished {
        void tcpConnectionEstablished();
    }
    TCPConnectionEstablished callBack;

    public static final int UDP_PORT = 2020;
    public static final int TCP_PORT = 2021;
    public static final int BIND_PORT = 2022;
    public static final int BUFF_LENGTH = 294;

    DatagramSocket udpSocket;
    byte[] buff;

    Socket tcpSocket;
    InputStream inputStream;
    OutputStream outputStream;

    private States currentState;
    private static SutoConnection instance = null;

    private String userName;
    private String hostName;
    private Inet4Address clientAddr;

    public static SutoConnection getInstance() {
        if(instance == null) {
            try {
                instance = new SutoConnection();
            } catch (SocketException e) {
                Log.e(TAG, "getInstance: Error in constructing SutoConnection object", e);
            }
        }
        return instance;
    }

    private SutoConnection() throws SocketException {
        udpSocket = new DatagramSocket(UDP_PORT);
        tcpSocket = new Socket();
        inputStream = null;
        outputStream = null;
        buff = new byte[BUFF_LENGTH];
        currentState = States.INACTIVE;
    }

    public void setTCPConnectionCallBack(TCPConnectionEstablished callBack) {
        this.callBack = callBack;
    }

    @Override
    public void run() {
        while(true) {
            currentState = States.ACTIVE;
            DatagramPacket packet = new DatagramPacket(buff, BUFF_LENGTH);
            try {
                udpSocket.receive(packet);
                String msg = StringUtils.substringBefore(new String(packet.getData(), StandardCharsets.UTF_8), "+");
                boolean validationRes = validateHelloMsg(msg, (Inet4Address) packet.getAddress());
                if (validationRes) {
                    // Open TCP connection to the client and return control to protocol checker somehow.
                    SocketAddress bindPoint = new InetSocketAddress(BIND_PORT);
                    SocketAddress sutoClientAddr = new InetSocketAddress(clientAddr, TCP_PORT);
                    tcpSocket.bind(bindPoint);
                    tcpSocket.connect(sutoClientAddr);
                    if (tcpSocket.isConnected()) {
                        currentState = States.TCP_CONNECTED;
                        Log.d(TAG, "run: TCP Connection established");
                        inputStream = tcpSocket.getInputStream();
                        outputStream = tcpSocket.getOutputStream();
                        callBack.tcpConnectionEstablished();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "run: Error reading data from suto client", e);
            }
        }
    }

    private boolean validateHelloMsg(String msg, Inet4Address sender) {
        if (StringUtils.startsWith(msg, "SUTO_")) {
            String[] msgParts = StringUtils.split(msg.substring(4), '@');
            if (msgParts.length == 2) {
                userName = msgParts[0];
                hostName = msgParts[1];
                clientAddr = sender;
                currentState = States.HELLO;
                Log.d(TAG, "validateHelloMsg: username- " + userName);
                Log.d(TAG, "validateHelloMsg: hostname- " + hostName);
                Log.d(TAG, "validateHelloMsg: sender- " + clientAddr.getHostAddress());
                return true;
            }
        }
        return false;
    }

    public boolean sendRequest(String request) {
        if (currentState != States.TCP_CONNECTED) {
            return false;
        }
        byte[] req = request.getBytes(StandardCharsets.UTF_8);
        try {
            outputStream.write(req);
        } catch (IOException e) {
            Log.e(TAG, "sendRequest: Error writing to stream", e);
            return false;
        }
        return true;
    }

    public String waitForMsg() {
        byte[] msg = new byte[50];
        int nBytes = 0;
        try {
            nBytes = inputStream.read(msg, 0, 50);
        } catch (IOException e) {
            Log.e(TAG, "waitForMsg: Cannot Read", e);
        }
        if (nBytes == 30) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < nBytes; i++) {
                builder.append((char)msg[i]);
            }
            return builder.toString();
        } else if(nBytes == 50) {
            return new String(msg, StandardCharsets.UTF_8);
        } else {
            Log.e(TAG, "waitForMsg: Invalid msg was read");
            return null;
        }
    }

    public States getCurrentState() {
        return currentState;
    }

    public String getUserName() {
        return userName;
    }

    public String getHostName() {
        return hostName;
    }
}
