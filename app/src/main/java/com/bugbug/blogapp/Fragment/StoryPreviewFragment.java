package com.bugbug.blogapp.Fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugbug.blogapp.Model.UserStories;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.Util.CloudinaryUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class StoryPreviewFragment extends Fragment {
    private ImageView previewImage;
    private Button btnPost, btnCancel;
    private Uri imageUri;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            imageUri = Uri.parse(getArguments().getString("imageUri"));
        }
        dialog = new ProgressDialog(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_preview, container, false);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        previewImage = view.findViewById(R.id.preview_image);
        btnPost = view.findViewById(R.id.btn_post);
        btnCancel = view.findViewById(R.id.btn_cancel);

        if (imageUri != null) {
            Picasso.get().load(imageUri).into(previewImage);
        } else {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }

        btnPost.setOnClickListener(v -> {
            uploadImageToFirebase(imageUri);
        });

        btnCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
    private void uploadImageToFirebase(Uri uri) {
        dialog.show();
        CloudinaryUtil.uploadImage(getContext(), uri, new CloudinaryUtil.UploadImageResultListener() {
            @Override
            public void onSuccess(String imageUrl) {
                long currentTime = new Date().getTime();
                UserStories userStory = new UserStories(imageUrl, currentTime);
                String storyId = database.getReference().push().getKey();

                database.getReference()
                        .child("stories")
                        .child(auth.getUid())
                        .child("storyAt")
                        .setValue(currentTime)
                        .addOnSuccessListener(unused -> {
                            database.getReference()
                                    .child("stories")
                                    .child(auth.getUid())
                                    .child("userStories")
                                    .child(storyId)
                                    .setValue(userStory)
                                    .addOnSuccessListener(aVoid -> {
                                        dialog.dismiss();
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    });
                        });
            }
            @Override
            public void onFailure(Exception e) {
                dialog.dismiss();
                Toast.makeText(getContext(), "Tải ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}