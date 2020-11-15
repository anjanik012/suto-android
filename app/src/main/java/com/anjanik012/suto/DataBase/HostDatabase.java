package com.anjanik012.suto.DataBase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Host.class}, version = 1)
public abstract class HostDatabase extends RoomDatabase {
    public abstract HostDao hostDao();

    private static volatile HostDatabase instance;
    private static final int NUM_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUM_THREADS);

    public static HostDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (HostDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), HostDatabase.class, "host_database")
//                            .addCallback(createDatabaseCallback)
                            .build();
                }
            }
        }
        return instance;
    }
//
//    private static RoomDatabase.Callback createDatabaseCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase database) {
//            super.onCreate(database);
//
//            databaseWriteExecutor.execute(()->{
//                HostDao systemDao = instance.hostDao();
//                systemDao.deleteAll();
//
//                Host host = new Host("root@mybox", "C++DonRamos", null);
//                systemDao.insert(host);
//            });
//        }
//    };
}
