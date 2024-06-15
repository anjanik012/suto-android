package com.anjanik012.suto.Backend;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.DataBase.HostRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import javax.net.SocketFactory;

import org.apache.commons.lang3.StringUtils;

public class SutoConnection {
    private static final String TAG = "SutoConnection";

    public enum States {
        INACTIVE, ACTIVE, HELLO, TCP_CONNECTED
    }

    private boolean listening = false;

    public static final int UDP_PORT = 2020;
    public static final int TCP_PORT = 2021;
    public static final int BIND_PORT = 2022;
    public static final int BUFF_LENGTH = 294;

    private DatagramSocket udpSocket;
    private final byte[] buff;

    private Socket tcpSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private States currentState;
    private static SutoConnection instance = null;

    private Inet4Address clientAddr;

    private HostRepository repository;
    private Protocol protocol;

    public static SutoConnection getInstance(Context context) {
        if (instance == null) {
            synchronized (SutoConnection.class) {
                if (instance == null) {
                    try {
                        instance = new SutoConnection(context);
                    } catch (SocketException e) {
                        Log.e(TAG, "getInstance: Error in constructing SutoConnection object", e);
                    }
                }
            }
        }
        return instance;
    }

    private SutoConnection(Context context) throws SocketException {
        udpSocket = new DatagramSocket(UDP_PORT);
        udpSocket.setReceiveBufferSize(BUFF_LENGTH);
        tcpSocket = null;
        inputStream = null;
        outputStream = null;
        buff = new byte[BUFF_LENGTH];
        currentState = States.INACTIVE;
        repository = HostRepository.getInstance(context);
        protocol = Protocol.getInstance(context);
    }

    public void start() {
        listening = true;
        DatagramPacket packet = new DatagramPacket(buff, BUFF_LENGTH);
        new Thread(() -> {
            while (listening) {
                if (currentState != States.TCP_CONNECTED) {
                    currentState = States.ACTIVE;
                    Log.d(TAG, "run: Start UDP listening thread");
                    Log.d(TAG, "run: Thread ID:- " + Thread.currentThread().getId());
                    try {
                        udpSocket.receive(packet);
                        String msg = StringUtils.substringBefore(new String(packet.getData(), StandardCharsets.UTF_8), "+");
                        Log.d(TAG, "run: UDP Message received:-" + msg);
                        Host host = validateHelloMsg(msg);
                        if (host != null) {
                            // Host entry is in database;
                            // Open TCP connection to the client
                            SocketFactory socketFactory = SocketFactory.getDefault();
                            try {
                                clientAddr = (Inet4Address) packet.getAddress();
                                tcpSocket = socketFactory.createSocket(clientAddr, TCP_PORT);
                            } catch (ConnectException connectException) {
                                Log.e(TAG, "start: suto client offline, this packet is probably an old packet...ignoring", connectException);
                                continue;
                            }
                            if (tcpSocket.isConnected()) {
                                currentState = States.TCP_CONNECTED;
                                Log.d(TAG, "run: TCP Connection established");
                                inputStream = tcpSocket.getInputStream();
                                outputStream = tcpSocket.getOutputStream();
                                protocol.init(host);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "run: Error reading data from suto client", e);
                    }
                }
            }
        }).start();
    }

    public void stop() {
        listening = false;
        try {
            tcpSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "stop: ", e);
        }
    }

    private Host validateHelloMsg(String msg) {
        if (StringUtils.startsWith(msg, "SUTO_")) {
            String key = msg.substring(5);
            Host host = repository.getHost(key);
            if (host != null) {
                currentState = States.HELLO;
                return host;
            }
        }
        return null;
    }

    public boolean sendRequest(String request) {
        if (currentState != States.TCP_CONNECTED) {
            return false;
        }
        byte[] req = request.getBytes(StandardCharsets.UTF_8);
        try {
            outputStream.write(req);
            outputStream.flush();
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
        if (nBytes == 30 || nBytes == 11) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < nBytes; i++) {
                builder.append((char) msg[i]);
            }
            return builder.toString();
        } else if (nBytes == 50) {
            return new String(msg, StandardCharsets.UTF_8);
        } else {
            Log.e(TAG, "waitForMsg: Invalid msg was read");
            return null;
        }
    }

    public void setCurrentState(States state) {
        synchronized (currentState) {
            currentState = state;
        }
    }

    public void closeTCP() throws IOException {
        synchronized (this) {
            tcpSocket.close();
            Log.d(TAG, "closeTCP: TCP Connection closed");
            currentState = States.ACTIVE;
        }
    }

    public States getCurrentState() {
        return currentState;
    }

}
