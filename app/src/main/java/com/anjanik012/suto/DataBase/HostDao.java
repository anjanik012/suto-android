package com.anjanik012.suto.DataBase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HostDao {
    @Query("SELECT * FROM host_database")
    LiveData<List<Host>> getAll();

    @Query("SELECT password FROM host_database WHERE hostName=:name")
    String getHostPassword(String name);

    @Query("SELECT salt FROM host_database WHERE hostName=:name")
    String getSalt(String name);

    @Query("SELECT * FROM host_database WHERE hostName=:name")
    Host getHost(String name);

    @Query("UPDATE host_database SET salt=:salt WHERE hostName=:name")
    int updateSalt(@NonNull String name, String salt);

    @Insert
    void insertAll(Host... hosts);

    @Insert
    void insert(Host host);

    @Delete
    void delete(Host host);

    @Query("DELETE FROM host_database")
    void deleteAll();
}
