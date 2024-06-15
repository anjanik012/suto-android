package com.anjanik012.suto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.UI.HostDialogFragment;
import com.anjanik012.suto.UI.HostListAdapter;
import com.anjanik012.suto.UI.HostViewModel;
import com.anjanik012.suto.UI.SwipeToDeleteCallback;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private HostViewModel hostViewModel;
    private FloatingActionButton fab;
    private TextView noHostView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bottom_bar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final HostListAdapter adapter = new HostListAdapter(new HostListAdapter.HostDiff(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        hostViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(HostViewModel.class);
        noHostView = findViewById(R.id.no_host_textview);

        hostViewModel.getHostNames().observe(this, hosts -> {
            if(hosts.isEmpty()) {
                noHostView.setVisibility(View.VISIBLE);
            } else {
                noHostView.setVisibility(View.INVISIBLE);
            }
            adapter.submitList(hosts);
        });
        fab = findViewById(R.id.add_host_fab);
        fab.setOnClickListener(v -> {
            HostDialogFragment dialogFragment = new HostDialogFragment();
            FragmentManager manager = getSupportFragmentManager();
            dialogFragment.show(manager, "Dialog");
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelsManager.getInstance(this);
        }
        Intent intent = new Intent(this, BackgroundService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent intent = new Intent(this, BackgroundService.class);
//        stopService(intent);
    }
}