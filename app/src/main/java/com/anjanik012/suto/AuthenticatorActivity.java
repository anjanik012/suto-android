package com.anjanik012.suto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.widget.Toast;

import com.anjanik012.suto.Backend.Protocol;

import java.util.concurrent.Executor;

public class AuthenticatorActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.cancel(254);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Protocol.getInstance(AuthenticatorActivity.this).onAuthenticationFailure();
                Toast.makeText(AuthenticatorActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Protocol.getInstance(AuthenticatorActivity.this).onAuthenticationSuccess();
                Toast.makeText(AuthenticatorActivity.this, "Authenticated", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Protocol.getInstance(AuthenticatorActivity.this).onAuthenticationFailure();
                Toast.makeText(AuthenticatorActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate Connection")
                .setSubtitle("From a known host")
                .setNegativeButtonText("Use Password")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}