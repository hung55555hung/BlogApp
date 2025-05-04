package com.bugbug.blogapp.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.Util.CloudinaryUtil;
import com.bugbug.blogapp.databinding.FragmentAddBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;


public class AddFragment extends Fragment {
    FragmentAddBinding binding;
    BottomNavigationView bottomNav;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri uri;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public AddFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        binding.imagePost.setImageURI(uri);
                        binding.postBtn.setEnabled(true);
                        binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn));
                        binding.postBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        database.getReference().child("Users")
                        .child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user=snapshot.getValue(User.class);
                        binding.userName.setText(user.getName());
                        binding.profession.setText(user.getProfession());
                        String coverPhoto = user.getCoverPhoto();
                        if (coverPhoto == null || coverPhoto.isEmpty()) {
                            Picasso.get()
                                    .load("https://i.pinimg.com/736x/bc/43/98/bc439871417621836a0eeea768d60944.jpg")
                                    .placeholder(R.drawable.avt)
                                    .into(binding.profileImage);
                        } else {
                            Picasso.get()
                                    .load(coverPhoto)
                                    .placeholder(R.drawable.avt)
                                    .into(binding.profileImage);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        binding = FragmentAddBinding.inflate(inflater, container, false);

        binding.postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String descriptionPost=binding.postDescription.getText().toString();
                if(!descriptionPost.isEmpty()) {
                    binding.postBtn.setEnabled(true);
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn));
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white));
                }else{
                    binding.postBtn.setEnabled(false);
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.following_btn));
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });
        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri!=null){
                    CloudinaryUtil.uploadImage(getContext(), uri, new CloudinaryUtil.UploadResultListener() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            if (imageUrl != null) {
                                Post post = new Post();
                                post.setPostImage(imageUrl);
                                post.setPostedBy(currentUser.getUid());
                                post.setPostDescription(binding.postDescription.getText().toString());
                                post.setPostedAt(new Date().getTime());
                                database.getReference().child("Posts")
                                        .push()
                                        .setValue(post)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                if (isAdded()) {
                                                    Toast.makeText(requireContext(), "Posting successfully", Toast.LENGTH_SHORT).show();
                                                }
                                                bottomNav.setSelectedItemId(R.id.nav_home);
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                bottomNav.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else{
                    Post post=new Post();
                    post.setPostImage("");
                    post.setPostedBy(currentUser.getUid());
                    post.setPostDescription(binding.postDescription.getText().toString());
                    post.setPostedAt(new Date().getTime());
                    database.getReference().child("Posts")
                            .push()
                            .setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Posting successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    bottomNav.setSelectedItemId(R.id.nav_home);
                                }
                            });

                }
            }
        });
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        View rootView = binding.getRoot();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!isAdded()){
                bottomNav.setVisibility(View.VISIBLE);
                return;
            }
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();

            if (heightDiff > getResources().getDisplayMetrics().density*200) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
    }
}