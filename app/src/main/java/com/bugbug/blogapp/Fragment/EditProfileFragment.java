package com.bugbug.blogapp.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.Util.CloudinaryUtil;
import com.bugbug.blogapp.databinding.FragmentEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri uri;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference userRef;
    private User user;

    public EditProfileFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        binding.profileImage.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);

        //Get user
        userRef=database.getReference("Users").child(currentUser.getUid());
        userRef.get().addOnSuccessListener(snapshot ->{
            if (snapshot.exists()) {
                user=snapshot.getValue(User.class);
                binding.editName.setText(user.getName());
                binding.editBirthday.setText(user.getBirthday());
                binding.editAddress.setText(user.getAddress());
                binding.editProfession.setText(user.getProfession());
                binding.editWorkAt.setText(user.getWorkAt());
                binding.editBio.setText(user.getBio());

                if (user.getCoverPhoto() != null && ! user.getCoverPhoto().isEmpty()) {
                    Picasso.get().load(user.getCoverPhoto()).placeholder(R.drawable.avatar_default).into(binding.profileImage);
                } else {
                    binding.profileImage.setImageResource(R.drawable.avatar_default);
                }
            }
        });

        //Button select image
        binding.editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

        //Button return click
        binding.btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        //Button save click
        binding.saveBtn.setOnClickListener(v -> {
            if(binding.editName.getText().toString().trim().isEmpty()){
                Toast.makeText(requireContext(), "Name's user cannot empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (uri != null) {
                CloudinaryUtil.uploadImage(requireContext(), uri, new CloudinaryUtil.UploadImageResultListener() {
                    @Override
                    public void onSuccess(String uploadedImageUrl) {
                        if(!user.getCoverPhoto().isEmpty()){
                            CloudinaryUtil.deleteImage(user.getCoverPhoto(),new CloudinaryUtil.DeleteImageResultListener(){
                                @Override
                                public void onSuccess() {
                                    user.setCoverPhoto(uploadedImageUrl);
                                    updateUserProfile();
                                }
                                @Override
                                public void onFailure(Exception e) {}
                            });
                        }else{
                            user.setCoverPhoto(uploadedImageUrl);
                            updateUserProfile();
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                updateUserProfile();
            }
        });
        return binding.getRoot();
    }
    private void updateUserProfile() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", binding.editName.getText().toString());
        userUpdates.put("birthday", binding.editBirthday.getText().toString());
        userUpdates.put("address", binding.editAddress.getText().toString());
        userUpdates.put("profession",binding.editProfession.getText().toString());
        userUpdates.put("workAt",binding.editWorkAt.getText().toString());
        userUpdates.put("bio", binding.editBio.getText().toString());
        userUpdates.put("coverPhoto", user.getCoverPhoto());

        userRef.updateChildren(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this.getContext(), "Updated", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}