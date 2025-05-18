package com.bugbug.blogapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugbug.blogapp.databinding.ActivityResetPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private FirebaseAuth mAuth;
    private CountDownTimer countDownTimer;
    private boolean canSendReset = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnSend.setOnClickListener(v -> sendResetEmail());
        binding.tvLogin.setOnClickListener(v -> login());

        updateSendButtonState();
    }

    private void sendResetEmail() {
        String email = binding.etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!canSendReset) {
            Toast.makeText(this, "Please wait before sending another request.", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        startCooldownTimer();
                    } else {
                        Toast.makeText(this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startCooldownTimer() {
        canSendReset = false;
        binding.btnSend.setEnabled(false);

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnSend.setText("Resend (" + (millisUntilFinished / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                canSendReset = true;
                updateSendButtonState();
            }
        }.start();
    }

    private void updateSendButtonState() {
        binding.btnSend.setEnabled(canSendReset);
        binding.btnSend.setText(canSendReset ? "Send" : "Resend (30s)");
    }

    private void login() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}