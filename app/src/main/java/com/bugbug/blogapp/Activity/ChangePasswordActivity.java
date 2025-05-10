package com.bugbug.blogapp.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugbug.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText oldPassword, oldPasswordAgain, newPassword;
    Button changePasswordBtn;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        oldPassword = findViewById(R.id.oldPassword);
        oldPasswordAgain = findViewById(R.id.oldPasswordAgain);
        newPassword = findViewById(R.id.newPassword);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);


        user = FirebaseAuth.getInstance().getCurrentUser();


        changePasswordBtn.setOnClickListener(v -> {
            String oldPass = oldPassword.getText().toString().trim();
            String oldPassAgain = oldPasswordAgain.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();


            if (oldPass.isEmpty() || oldPassAgain.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!oldPass.equals(oldPassAgain)) {
                Toast.makeText(this, "Old passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }


            user.updatePassword(newPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Toast.makeText(this, "Password Updated", Toast.LENGTH_SHORT).show();


                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(user.getUid());


                    userRef.child("password").setValue(newPass).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(this, "Password updated in database", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Password update failed in database", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                } else {
                    Toast.makeText(this, "Failed to Update Password", Toast.LENGTH_SHORT).show();
                }
            });
        });
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

    }
}
