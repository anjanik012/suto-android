package com.anjanik012.suto.Backend;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.anjanik012.suto.AuthenticatorActivity;
import com.anjanik012.suto.BackgroundService;
import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.DataBase.HostRepository;
import com.anjanik012.suto.NotificationChannelsManager;
import com.anjanik012.suto.R;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.digest.Crypt;

import java.io.IOException;

public class Protocol {
    private static final String TAG = "Protocol";

    private SutoConnection sutoConnection;
    private Host host;

    private enum ProtocolMode {
        SALT_ALL, SALT_RSALT
    }

    private ProtocolMode mode;
    private Context context;

    private String salt;
    private String rSalt;

    private static Protocol instance;

    public static Protocol getInstance(Context context) {
        if (instance == null) {
            synchronized (Protocol.class) {
                if (instance == null) {
                    instance = new Protocol(context);
                }
            }
        }
        return instance;
    }

    private Protocol(Context context) {
        this.context = context;
    }

    public void init(@NonNull Host host) {
        // Start protocol processing
        // Check if username and password exists in data-base
        Log.d(TAG, "init: Thread id:- " + Thread.currentThread().getId());
        this.host = host;
        sutoConnection = SutoConnection.getInstance(context);
        if (this.host.getSalt() != null) {
            Log.d(TAG, "init: Sending GET_RSALT");
            sutoConnection.sendRequest("SUTO_C_GET_RSALT");
            mode = ProtocolMode.SALT_RSALT;
        } else {
            Log.d(TAG, "init: Sending GET_SALT");
            sutoConnection.sendRequest("SUTO_C_GET_SALTA");
            mode = ProtocolMode.SALT_ALL;
        }
        Log.d(TAG, "init: Waiting for msg");
        String reply = sutoConnection.waitForMsg();
        salt = host.getSalt();
        rSalt = null;
        switch (mode) {
            case SALT_ALL:
                // Assuming MD5 hashes are not used anymore in Linux Distributions.
                salt = StringUtils.substring(reply, 11, 30);
                rSalt = StringUtils.substring(reply, 31, 50);
                break;
            case SALT_RSALT:
                rSalt = StringUtils.substring(reply, 11, 30);
                break;
        }
        // Correct Place for hookup for authentication
        /*
        TODO: Send a notification of authorization here and open Authorization UI.
        TODO: If it succeeds then send the final hash if not then send failure message.
        */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannelsManager.authenticationChannelId);
        builder.setContentTitle(context.getString(R.string.authentication_notif_title));
        builder.setContentText("From: " + host.getHostName());
        builder.setSmallIcon(R.drawable.ic_notif_white);

        Intent intent = new Intent(context, AuthenticatorActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.addAction(R.drawable.ic_notif_white, context.getString(R.string.authentication_notif_auth), pendingIntent);
        builder.setAutoCancel(true);

        Intent cancelIntent = new Intent(BackgroundService.AUTH_CANCEL_ACTION);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0);
        builder.setDeleteIntent(cancelPendingIntent);
        builder.addAction(R.drawable.ic_notif_white, context.getString(R.string.authentication_notif_cancel), cancelPendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(254, builder.build());
    }

    public void onAuthenticationSuccess() {
        Thread thread = new Thread(()->{
            String key = host.getPassWord();
            String finalHash = Crypt.crypt(Crypt.crypt(key, salt), rSalt);
            sutoConnection.sendRequest("SUTO_CF_HASH_" + finalHash);
            String authStatus = sutoConnection.waitForMsg();
            if (authStatus.equals("SUTO_AUTH_1")) {
                Log.d(TAG, "init: Auth Success");
            } else if (authStatus.equals("SUTO_AUTH_0")) {
                Log.d(TAG, "init: Auth Failed");
            }
            try {
                sutoConnection.closeTCP();
            } catch (IOException e) {
                Log.e(TAG, "onAuthenticationSuccess: Unable to close connection", e);
            }
            if (mode == ProtocolMode.SALT_ALL) {
                HostRepository.getInstance(context).updateSalt(host.getHostName(), salt);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "onAuthenticationSuccess: Some thread exception", e);
        }
    }

    public void onAuthenticationFailure() {
        new Thread(() -> {
            sutoConnection.sendRequest("SUTO_AUTH_CANCELLED");
            try {
                sutoConnection.closeTCP();
            } catch (IOException e) {
                Log.e(TAG, "onAuthenticationFailure: cannot close TCP connection", e);
            }
        }).start();
    }
}
