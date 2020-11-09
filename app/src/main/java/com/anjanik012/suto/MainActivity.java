package com.anjanik012.suto;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.anjanik012.suto.Backend.Protocol;
import com.anjanik012.suto.Backend.SutoConnection;

public class MainActivity extends AppCompatActivity {

    private SutoConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connection = SutoConnection.getInstance();
        connection.setTCPConnectionCallBack(() -> {
            // Initiate protocol
            Protocol p = new Protocol();
            p.init();
        });
        Thread t = new Thread(connection);
        t.start();
    }
}