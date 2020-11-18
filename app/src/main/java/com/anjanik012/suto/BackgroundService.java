package com.anjanik012.suto;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.anjanik012.suto.Backend.Protocol;
import com.anjanik012.suto.Backend.SutoConnection;
import com.anjanik012.suto.DataBase.Host;

public class BackgroundService extends LifecycleService {
    private static final String TAG = "BackgroundService";
    public static final int FOREGROUND_NOTIFICATION_ID = 255;
    public static final String AUTH_CANCEL_ACTION = "com.anjanik012.suto.AUTH_CANCEL";

    private SutoConnection connection;
    private AuthenticationCancelReceiver authenticationCancelReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

// Notification ID cannot be 0.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_NOTIFICATION_ID, createNotification());
        }

        connection = SutoConnection.getInstance(getApplication());
        connection.start();
        authenticationCancelReceiver = new AuthenticationCancelReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AUTH_CANCEL_ACTION);
        registerReceiver(authenticationCancelReceiver, filter);
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
        unregisterReceiver(authenticationCancelReceiver);
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NotificationChannelsManager.foregroundChannelId);
        builder.setSmallIcon(R.drawable.ic_notif_white)
                .setContentIntent(pendingIntent)
                .setContentText(getText(R.string.foreground_notification_msg))
                .setPriority(Notification.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE);
        return builder.build();
    }
}
