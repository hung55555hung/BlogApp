package com.bugbug.blogapp.Fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bugbug.blogapp.Adapter.PostAdapter;
import com.bugbug.blogapp.Adapter.StoryAdapter;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.Story;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.Model.UserStories;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.FragmentHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    ArrayList<Story> storyList;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ActivityResultLauncher<String> galleryLauncher;
    ActivityResultLauncher<Uri> cameraLauncher;
    ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private View loadingOverlay;
    private static final long STORY_EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    private Uri photoUri;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                photoUri = uri;
                navigateToPreview(photoUri);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && photoUri != null) {
                navigateToPreview(photoUri);
            }
        });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        storyList = new ArrayList<>();
        StoryAdapter storyAdapter = new StoryAdapter(storyList, getContext(), this::showImageSourceDialog);
        LinearLayoutManager storyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.storyRV.setLayoutManager(storyLayoutManager);
        binding.storyRV.setNestedScrollingEnabled(false);
        binding.storyRV.setAdapter(storyAdapter);
        storyList.add(new Story(R.drawable.im1));

        database.getReference().child("Users")
                .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
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
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        binding.profileImage.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_profile);
        });

        loadStories(storyAdapter);

        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager postLayoutManager = new LinearLayoutManager(getContext());
        binding.dashboardRV.setLayoutManager(postLayoutManager);
        binding.dashboardRV.setNestedScrollingEnabled(false);
        binding.dashboardRV.setAdapter(postAdapter);

        showLoadingOverlay();

        database.getReference().child("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                Collections.shuffle(postList);
                postAdapter.notifyDataSetChanged();
                hideLoadingOverlay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingOverlay();
            }
        });

        return view;
    }

    private void showImageSourceDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_image_source, null);
        bottomSheetDialog.setContentView(dialogView);

        View cameraOption = dialogView.findViewById(R.id.option_camera);
        cameraOption.setOnClickListener(v -> {
            checkCameraPermission();
            bottomSheetDialog.dismiss();
        });

        View galleryOption = dialogView.findViewById(R.id.option_gallery);
        galleryOption.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        ContentResolver resolver = requireContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        photoUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (photoUri != null) {
            cameraLauncher.launch(photoUri);
        } else {
            Toast.makeText(getContext(), "Unable to create URI to save the photo!", Toast.LENGTH_LONG).show();
        }
    }

    private void loadStories(StoryAdapter storyAdapter) {
        String currentUserId = auth.getUid();
        database.getReference().child("Following").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> followedUsers = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot followSnapshot : snapshot.getChildren()) {
                                String followedUserId = followSnapshot.getKey();
                                followedUsers.add(followedUserId);
                            }
                        }
                        followedUsers.add(currentUserId);
                        if (followedUsers.isEmpty()) {
                            storyAdapter.notifyDataSetChanged();
                            return;
                        }
                        database.getReference().child("stories").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                storyList.clear();
                                storyList.add(new Story(R.drawable.im1));
                                if (snapshot.exists()) {
                                    for (String followedUserId : followedUsers) {
                                        DataSnapshot userSnapshot = snapshot.child(followedUserId);
                                        if (!userSnapshot.exists()) {
                                            continue;
                                        }

                                        Long storyAt = userSnapshot.child("storyAt").getValue(Long.class);
                                        ArrayList<UserStories> stories = new ArrayList<>();
                                        for (DataSnapshot storySnapshot : userSnapshot.child("userStories").getChildren()) {
                                            UserStories userStory = storySnapshot.getValue(UserStories.class);
                                            if (userStory != null && !isStoryExpired(userStory.getStoryAt())) {
                                                stories.add(userStory);
                                            }
                                        }
                                        if (!stories.isEmpty()) {
                                            Story story = new Story();
                                            story.setStoryBy(followedUserId);
                                            story.setStoryAt(storyAt);
                                            story.setStories(stories);
                                            storyList.add(story);
                                        }
                                    }
                                    storyAdapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void navigateToPreview(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString("imageUri", uri.toString());

        StoryPreviewFragment fragment = new StoryPreviewFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private boolean isStoryExpired(long storyAt) {
        return (new Date().getTime() - storyAt) > STORY_EXPIRATION_TIME;
    }
    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
}