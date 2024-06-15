package com.anjanik012.suto.DataBase;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HostRepository {

    private static final String TAG = "HostRepository";
    private static HostRepository instance;
    private HostDatabase database;
    private HostDao dao;
    private LiveData<List<Host>> hostNames;
//    private HashMap<String, String> hostNameCache;

    public interface InsertCallback{
        void insertHostCallback(boolean result);
    }

    public static HostRepository getInstance(Context context) {
        if(instance == null) {
            instance = new HostRepository(context);
        }
        return instance;
    }

    private HostRepository(Context context) {
        database = HostDatabase.getInstance(context);
        dao = database.hostDao();
        hostNames = dao.getAll();
//        hostNameCache = new HashMap<>();
//        for (Host host : hostNames.getValue()) {
//            if (!hostNameCache.containsKey(host.getHostName())) {
//                hostNameCache.put(host.getHostName(), host.getSalt());
//            }
//        }
//        hostNames.observeForever(hosts -> {
//            for (Host host : hosts) {
//                String name = host.getHostName();
//                String salt = host.getSalt();
//                try {
//                    if (!hostNameCache.containsKey(name)) {
//                        hostNameCache.put(name, salt);
//                    }
//                } catch (NullPointerException e) {
//                    Log.e(TAG, "HostRepository: null object", e);
//                }
//            }
//        });
    }

    public LiveData<List<Host>> getHostNames() {
        return hostNames;
    }

    // Run on Non UI Thread
    public void insert(Host host) {
        HostDatabase.databaseWriteExecutor.execute(()->{
            Host h = dao.getHost(host.getHostName());
            if(h == null || !h.equals(host)) {
                dao.insert(host);
            }
        });
    }

    public void insert(Host host, InsertCallback callback) {
        HostDatabase.databaseWriteExecutor.execute(()->{
            Host h = dao.getHost(host.getHostName());
            if (h == null || !h.equals(host)) {
                dao.insert(host);
                callback.insertHostCallback(true);
            } else {
                callback.insertHostCallback(false);
            }
        });
    }

    public void updateSalt(@NonNull String host, String salt) {
        HostDatabase.databaseWriteExecutor.execute(()->{
            dao.updateSalt(host, salt);
        });
    }

    public void deleteHost(Host host) {
        HostDatabase.databaseWriteExecutor.execute(()->{
            dao.delete(host);
        });
    }
    public String getPassword(@NonNull String hostName) {
        return dao.getHostPassword(hostName);
    }

    public String getSalt(@NonNull String hostName) {
        return dao.getSalt(hostName);
//        return hostNameCache.get(hostName);
    }

    public Host getHost(@NonNull String hostName) {
        return dao.getHost(hostName);
    }
}
