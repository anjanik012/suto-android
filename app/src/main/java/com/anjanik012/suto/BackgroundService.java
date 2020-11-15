package com.anjanik012.suto;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleService;

import com.anjanik012.suto.Backend.Protocol;
import com.anjanik012.suto.Backend.SutoConnection;
import com.anjanik012.suto.DataBase.Host;

public class BackgroundService extends LifecycleService {
    private static final String TAG = "BackgroundService";
    public static final int FOREGROUND_NOTIFICATION_ID = 255;

    private SutoConnection connection;

    @Override
    public void onCreate() {
        super.onCreate();

// Notification ID cannot be 0.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_NOTIFICATION_ID, createNotification());
        }

        connection = SutoConnection.getInstance(getApplication());
        connection.setTCPConnectionCallBack(host -> {
                 Protocol protocol = new Protocol(getApplication(), host);
                 protocol.init();
        });
        connection.start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        connection.stop();
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Notification createNotification() {
        String NOTIFICATION_CHANNEL_ID = "com.anjanik012.suto";
        String CHANNEL_NAME = "ForegroundService";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setContentTitle("SUTO")
                .setContentText("Listening for UDP")
                .setPriority(Notification.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE);
        return builder.build();
    }
}
