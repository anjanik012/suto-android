package com.anjanik012.suto.UI;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.DataBase.HostRepository;
import com.anjanik012.suto.R;
import com.google.android.material.snackbar.Snackbar;

public class HostListAdapter extends ListAdapter<Host, HostViewHolder> {

    private Host recentlyDeletedItem;
    private int recentlyDeletedItemPosition;
    private Activity activity;

    public HostListAdapter(@NonNull DiffUtil.ItemCallback<Host> diffCallback, @NonNull Activity activity) {
        super(diffCallback);
        this.activity = activity;
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return HostViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        Host host = getItem(position);
        holder.bind(host.getHostName());
    }

    public Context getContext() {
        return activity.getApplicationContext();
    }

    public void deleteItem(int position) {
        recentlyDeletedItem = getItem(position);
        recentlyDeletedItemPosition = position;
        HostRepository.getInstance(activity.getApplication()).deleteHost(recentlyDeletedItem);
        showUndoSnackBar();
    }

    private void showUndoSnackBar() {
        View view = activity.findViewById(R.id.main_bottom_bar);
        Snackbar snackbar = Snackbar.make(view, R.string.host_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo_delete_item, v->undoDelete());
        snackbar.show();
    }

    private void undoDelete() {
        HostRepository.getInstance(activity.getApplication()).insert(recentlyDeletedItem);
    }

    public static class HostDiff extends DiffUtil.ItemCallback<Host> {

        @Override
        public boolean areItemsTheSame(@NonNull Host oldItem, @NonNull Host newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Host oldItem, @NonNull Host newItem) {
            return oldItem.getHostName().equals(newItem.getHostName());
        }
    }
}
