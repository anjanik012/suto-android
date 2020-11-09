package com.anjanik012.suto.Backend;

public class Protocol {
    private static final String TAG = "Protocol";

    private SutoConnection sutoConnection;
    private String userName;
    private String hostName;

    public Protocol() {
        sutoConnection = SutoConnection.getInstance();
        userName = sutoConnection.getUserName();
        hostName = sutoConnection.getHostName();
    }

    public void init() {
        sutoConnection.sendRequest("Hello");
    }
}
