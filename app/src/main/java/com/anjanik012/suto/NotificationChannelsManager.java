package com.anjanik012.suto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

public class NotificationChannelsManager {
    private static final String TAG = "NotifChannelManager";

    private NotificationChannel foregroundServiceChannel;
    private NotificationChannel authenticationChannel;

    public static final String foregroundChannelId = "com.anjanik012.suto.Foreground";
    public static final String authenticationChannelId = "com.anjanik012.suto.Authentication";

    private static final String foregroundChannelName = "ForegroundService";
    private static final String authenticationChannelName = "Authentication Notifier";

    private static final String foregroundChannelDescription = "Listener for authentication requests";
    private static final String authenticationChannelDescription = "Confirm Authentication Notifier";

    private static NotificationChannelsManager instance;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannelsManager getInstance(Context ct) {
        if (instance == null) {
            synchronized (NotificationChannelsManager.class) {
                if (instance == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        instance = new NotificationChannelsManager(ct);
                    }
                }
            }
        }
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannelsManager(Context ct) {
        foregroundServiceChannel = new NotificationChannel(foregroundChannelId, foregroundChannelName, NotificationManager.IMPORTANCE_LOW);
        authenticationChannel = new NotificationChannel(authenticationChannelId, authenticationChannelName, NotificationManager.IMPORTANCE_HIGH);

        foregroundServiceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        foregroundServiceChannel.setDescription(foregroundChannelDescription);

        authenticationChannel.setLightColor(Color.BLUE);
        authenticationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        authenticationChannel.setDescription(authenticationChannelDescription);

        NotificationManagerCompat manager = NotificationManagerCompat.from(ct);
        manager.createNotificationChannel(foregroundServiceChannel);
        manager.createNotificationChannel(authenticationChannel);
    }

    public NotificationChannel getForegroundServiceChannel() {
        return foregroundServiceChannel;
    }

    public NotificationChannel getAuthenticationChannel() {
        return authenticationChannel;
    }

}
