package com.anjanik012.suto.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.DataBase.HostRepository;

import java.util.List;

public class HostViewModel extends AndroidViewModel {

    private HostRepository repository;
    private LiveData<List<Host>> hostNames;

    public HostViewModel(@NonNull Application application) {
        super(application);
        repository = HostRepository.getInstance(application);
        hostNames = repository.getHostNames();
    }

    public void insert(Host host) {
        repository.insert(host);
    }

    public LiveData<List<Host>> getHostNames() {
        return hostNames;
    }
}
