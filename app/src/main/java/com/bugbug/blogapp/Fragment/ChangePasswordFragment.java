package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.FragmentChangePasswordBinding;
import com.bugbug.blogapp.databinding.FragmentEditProfileBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public ChangePasswordFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);

        //Button return
        binding.btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        //Button update
        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = binding.oldPassword.getText().toString().trim();
                String newPass = binding.newPassword.getText().toString().trim();
                String newPassAgain = binding.newPasswordAgain.getText().toString().trim();
                if (oldPass.isEmpty() || newPass.isEmpty() || newPassAgain.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPass.length() < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(newPassAgain)) {
                    Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = currentUser.getEmail();
                AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);
                currentUser.reauthenticate(credential).addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        currentUser.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(requireContext(), "Password Updated", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new ProfileFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return binding.getRoot();
    }
}