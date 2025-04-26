package com.bugbug.blogapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bugbug.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPasswordAgain;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordAgain = findViewById(R.id.etPasswordAgain);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bắt sự kiện khi bấm nút LOGIN (ở đây là nút Đăng ký)
        btnLogin.setText("REGISTER"); // đổi chữ LOGIN thành REGISTER
        btnLogin.setOnClickListener(v -> registerUser());

        // Bấm vào "Register!" sẽ quay lại màn Login
        tvRegister.setOnClickListener(v -> {
            finish(); // Quay lại LoginActivity
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordAgain = etPasswordAgain.getText().toString().trim();

        // Kiểm tra nhập liệu
        if (email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordAgain)) {
            Toast.makeText(this, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi Firebase để tạo tài khoản
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đăng ký xong, quay về màn Login
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. Thử lại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
