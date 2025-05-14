package com.bugbug.blogapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all the required information.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser!=null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


}
