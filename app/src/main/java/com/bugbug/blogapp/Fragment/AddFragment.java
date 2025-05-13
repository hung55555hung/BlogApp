package com.bugbug.blogapp.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bugbug.blogapp.Adapter.ImageAdapter;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.Util.CloudinaryUtil;
import com.bugbug.blogapp.databinding.FragmentAddBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFragment extends Fragment {
    FragmentAddBinding binding;
    BottomNavigationView bottomNav;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public AddFragment() {
    }

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
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                                imageUris.add(uri);
                            }
                        } else if (result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            imageUris.add(uri);
                        }
                        imageAdapter.notifyDataSetChanged();
                        updatePostButtonState();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);

        imageAdapter = new ImageAdapter(requireContext(), imageUris, position -> {
            imageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
            updatePostButtonState();
        });
        binding.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.imagesRecyclerView.setAdapter(imageAdapter);

        database.getReference().child("Users")
                .child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        binding.userName.setText(user.getName());
                        binding.profession.setText(user.getProfession());
                        String coverPhoto = user.getCoverPhoto();
                        if (coverPhoto == null || coverPhoto.isEmpty()) {
                            binding.profileImage.setImageResource(R.drawable.avatar_default);
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

        binding.postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePostButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnAddImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        binding.postBtn.setOnClickListener(v -> uploadPost());

        return binding.getRoot();
    }

    private void updatePostButtonState() {
        String description = binding.postDescription.getText().toString();
        boolean isEnabled = !description.isEmpty() || !imageUris.isEmpty();
        binding.postBtn.setEnabled(isEnabled);
        binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                isEnabled ? R.drawable.follow_btn : R.drawable.non_activate_btn));
        binding.postBtn.setTextColor(ContextCompat.getColor(requireContext(),
                isEnabled ? R.color.white : R.color.gray));
    }

    private void uploadPost() {
        String userId = currentUser.getUid();
        String postDescription = binding.postDescription.getText().toString();
        long postedAt = new Date().getTime();

        DatabaseReference postRef = database.getReference().child("Posts").push();
        String postId = postRef.getKey();

        Post post = new Post();
        post.setPostId(postId);
        post.setPostedBy(userId);
        post.setPostDescription(postDescription);
        post.setPostedAt(postedAt);

        if (!imageUris.isEmpty()) {
            CloudinaryUtil.uploadPostImages(requireContext(), imageUris, new CloudinaryUtil.UploadPostImagesListener() {
                @Override
                public void onSuccess(List<String> imageUrls) {
                    post.setPostImages(new ArrayList<>(imageUrls));
                    savePost(post, postId);
                }

                @Override
                public void onFailure(Exception e, int failedIndex) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Upload failed for image " + (failedIndex + 1) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        bottomNav.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            post.setPostImages(new ArrayList<>());
            savePost(post, postId);
        }
    }

    private void savePost(Post post, String postId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/Posts/" + postId, post);
        updates.put("/UserPosts/" + currentUser.getUid() + "/" + postId, true);

        database.getReference().updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Posting successfully", Toast.LENGTH_SHORT).show();
                    }
                    bottomNav.setSelectedItemId(R.id.nav_home);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Post failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        View rootView = binding.getRoot();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!isAdded()) {
                bottomNav.setVisibility(View.VISIBLE);
                return;
            }
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff > getResources().getDisplayMetrics().density * 200) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CloudinaryUtil.shutdown();
    }
}