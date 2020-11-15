package com.anjanik012.suto.UI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anjanik012.suto.R;

import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

public class HostViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "HostViewHolder";
    private final TextView hostNameView;
    private final TextView userNameView;

    public HostViewHolder(@NonNull View itemView) {
        super(itemView);
        hostNameView = itemView.findViewById(R.id.hostname);
        userNameView = itemView.findViewById(R.id.username);
    }

    public void bind(String text) {
            try {
                String[] strings = StringUtils.split(text, '@');
                userNameView.setText(strings[0]);
                hostNameView.setText(strings[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "bind: text:- " + text, e);
            }
    }

    static HostViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.host_item, parent, false);
        return new HostViewHolder(view);
    }

}
