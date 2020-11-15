package com.anjanik012.suto.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.anjanik012.suto.DataBase.Host;
import com.anjanik012.suto.DataBase.HostRepository;
import com.anjanik012.suto.R;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

public class HostDialogFragment extends DialogFragment implements HostRepository.InsertCallback{
    private TextInputEditText userName;
    private TextInputEditText hostName;
    private TextInputEditText passWord;

    private Button addButton;
    private Button cancelButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = activity.getLayoutInflater().inflate(R.layout.add_host_layout, null);
        userName = view.findViewById(R.id.username_input);
        hostName = view.findViewById(R.id.hostname_input);
        passWord = view.findViewById(R.id.password_input);
        addButton = view.findViewById(R.id.dialog_add_button);
        cancelButton = view.findViewById(R.id.dialog_cancel_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        addButton.setOnClickListener(v -> {
            String user = userName.getText().toString();
            String host = hostName.getText().toString();
            String pass = passWord.getText().toString();
            if (!StringUtils.isBlank(user) && !StringUtils.isBlank(host) && !StringUtils.isBlank(pass)) {
                Host h = new Host(user+"@"+host, pass, null);
                HostRepository.getInstance(activity.getApplication()).insert(h, this);
            } else {
                Toast.makeText(getContext(), "Empty field", Toast.LENGTH_SHORT).show();
            }
        });
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });
        return builder.create();
    }

    // Toast has to run on UI Thread.
    @Override
    public void insertHostCallback(boolean result) {
        if (result) {
            dismiss();
        } else {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), R.string.host_already_present, Toast.LENGTH_SHORT).show());
        }
    }
}
