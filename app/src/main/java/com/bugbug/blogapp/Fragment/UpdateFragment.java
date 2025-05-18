package com.bugbug.blogapp.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.bugbug.blogapp.databinding.FragmentUpdateBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateFragment extends Fragment {
    FragmentUpdateBinding binding;
    BottomNavigationView bottomNav;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    Post post;

    ProgressDialog dialog;

    public UpdateFragment() {
    }

    public static UpdateFragment newInstance(Post post) {
        UpdateFragment fragment = new UpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("post", post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            post = (Post) getArguments().getSerializable("post");
        }

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
        binding = FragmentUpdateBinding.inflate(inflater, container, false);
        setupUI();
        return binding.getRoot();
    }
    private void setupUI(){
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
                                    .placeholder(R.drawable.avatar_default)
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
        if (post != null) {
            binding.postDescription.setText(post.getPostDescription());
            if (post.getPostImages() != null) {
                for (String url : post.getPostImages()) {
                    imageUris.add(Uri.parse(url));
                }
                imageAdapter.notifyDataSetChanged();
            }
        }

        binding.btnAddImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        binding.postBtn.setOnClickListener(v -> updatePost());
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

    private void updatePost() {
        dialog = new ProgressDialog(requireContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Updating post");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();

        List<String> oldImageUrls = post.getPostImages() != null ? new ArrayList<>(post.getPostImages()) : new ArrayList<>();
        List<String> keptImageUrls = new ArrayList<>();
        List<Uri> newImageUris = new ArrayList<>();

        for (Uri uri : imageUris) {
            if (uri.toString().startsWith("http")) {
                keptImageUrls.add(uri.toString());
            } else {
                newImageUris.add(uri);
            }
        }

        List<String> deletedImages = new ArrayList<>(oldImageUrls);
        deletedImages.removeAll(keptImageUrls);

        if (!deletedImages.isEmpty()) {
            CloudinaryUtil.deleteImages(deletedImages, new CloudinaryUtil.DeleteImagesResultListener() {
                @Override
                public void onSuccess() {
                    savePost(keptImageUrls, newImageUris);
                }

                @Override
                public void onFailure(List<Exception> errors) {
                    Toast.makeText(getContext(), "Some images failed to delete", Toast.LENGTH_SHORT).show();
                    savePost(keptImageUrls, newImageUris);
                }
            });
        } else {
            savePost(keptImageUrls, newImageUris);
        }
    }


    private void savePost(List<String> keptImageUrls, List<Uri> newImageUris) {
        String userId = currentUser.getUid();
        String postDescription = binding.postDescription.getText().toString();
        long postedAt = new Date().getTime();

        post.setPostedBy(userId);
        post.setPostDescription(postDescription);
        post.setPostedAt(postedAt);

        if (!newImageUris.isEmpty()) {
            CloudinaryUtil.uploadPostImages(requireContext(), newImageUris, new CloudinaryUtil.UploadPostImagesListener() {
                @Override
                public void onSuccess(List<String> uploadedUrls) {
                    ArrayList<String> allImages = new ArrayList<>(keptImageUrls);
                    allImages.addAll(uploadedUrls);
                    updatePostInDatabase(allImages);
                }

                @Override
                public void onFailure(Exception e, int failedIndex) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Upload failed for image " + (failedIndex + 1), Toast.LENGTH_SHORT).show();
                        bottomNav.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                }
            });
        } else {
            updatePostInDatabase(new ArrayList<>(keptImageUrls));
        }
    }

    private void updatePostInDatabase(ArrayList<String> imageUrls) {
        post.setPostImages(imageUrls);
        Map<String, Object> updates = new HashMap<>();
        updates.put("postDescription", post.getPostDescription());
        updates.put("postImages", imageUrls);

        database.getReference().child("Posts").child(post.getPostId())
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                    bottomNav.setSelectedItemId(R.id.nav_home);
                });
        dialog.dismiss();
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
    }
}