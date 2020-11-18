package com.anjanik012.suto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.anjanik012.suto.Backend.Protocol;

public class AuthenticationCancelReceiver extends BroadcastReceiver {
    private static final String TAG = "AuthCancelReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(254);
        Protocol.getInstance(context).onAuthenticationFailure();
    }
}
