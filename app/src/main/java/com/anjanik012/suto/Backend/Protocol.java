package com.anjanik012.suto.Backend;

import android.app.Application;
import android.util.Log;

import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.DataBase.HostRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.digest.Crypt;

public class Protocol {
    private static final String TAG = "Protocol";

    private SutoConnection sutoConnection;
    private final Host host;

    private enum ProtocolMode{
        SALT_ALL, SALT_RSALT
    }
    private ProtocolMode mode;
    private HostRepository repository;

    public Protocol(Application application, Host host) {
        sutoConnection = SutoConnection.getInstance(application);
        repository = HostRepository.getInstance(application);
        this.host = host;
    }

    private boolean isSaltPresent() {
        return host.getSalt() != null;
    }
    public void init() {
        // Start protocol processing
        // Check if username and password exists in data-base
        Log.d(TAG, "init: Thread id:- " + Thread.currentThread().getId());
        if (isSaltPresent()) {
            sutoConnection.sendRequest("SUTO_C_GET_RSALT");
            mode = ProtocolMode.SALT_RSALT;
        } else {
            sutoConnection.sendRequest("SUTO_C_GET_SALTA");
            mode = ProtocolMode.SALT_ALL;
        }
        String reply = sutoConnection.waitForMsg();
        String salt = host.getSalt();
        String rSalt = null;
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
        String key = host.getPassWord();
        String finalHash = Crypt.crypt(Crypt.crypt(key, salt), rSalt);
        sutoConnection.sendRequest("SUTO_CF_HASH_" + finalHash);
        String authStatus = sutoConnection.waitForMsg();
        if (authStatus.equals("SUTO_AUTH_1")) {
            Log.d(TAG, "init: Auth Success");
        } else if (authStatus.equals("SUTO_AUTH_0")){
            Log.d(TAG, "init: Auth Failed");
        }
        if (mode == ProtocolMode.SALT_ALL) {
            repository.updateSalt(host.getHostName(), salt);
        }
    }
}
