package com.anjanik012.suto.DataBase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "host_database")
public class Host {
    @PrimaryKey
    @NonNull
    private String hostName;

    @ColumnInfo(name = "password")
    @NonNull
    private String passWord;

    @ColumnInfo(name = "salt")
    private String salt;

    public Host(@NonNull String hostName, @NonNull String passWord, String salt) {
        this.hostName = hostName;
        this.passWord = passWord;
        this.salt = salt;
    }

    @NonNull
    public String getHostName() {
        return hostName;
    }

    @NonNull
    public String getPassWord() {
        return passWord;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setHostName(@NonNull String hostName) {
        this.hostName = hostName;
    }

    public void setPassWord(@NonNull String passWord) {
        this.passWord = passWord;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Host && obj != null) {
            Host host = (Host) obj;
            return this.hostName.equals(host.getHostName());
        }
        return false;
    }
}
