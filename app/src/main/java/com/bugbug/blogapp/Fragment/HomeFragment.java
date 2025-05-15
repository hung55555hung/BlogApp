package com.bugbug.blogapp.Fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bugbug.blogapp.Adapter.PostAdapter;
import com.bugbug.blogapp.Adapter.StoryAdapter;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.Story;
import com.bugbug.blogapp.Model.UserStories;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.Util.CloudinaryUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;


public class HomeFragment extends Fragment {

    RecyclerView storyRecyclerView, dasboardRecyclerView;
    ArrayList<Story> storyList;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    ShapeableImageView addStory;
    ActivityResultLauncher<String> galleryLauncher;
    ProgressDialog dialog;
    String sharedBy;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageToFirebase(uri);
                    }
                });

        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);


        storyRecyclerView = view.findViewById(R.id.storyRV);
        storyList = new ArrayList<>();
        StoryAdapter storyAdapter = new StoryAdapter(storyList, getContext(), () -> {
            galleryLauncher.launch("image/*");
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyRecyclerView.setLayoutManager(linearLayoutManager);
        storyRecyclerView.setNestedScrollingEnabled(false);
        storyRecyclerView.setAdapter(storyAdapter);
        // Thêm item "Create a Story"
        storyList.add(new Story(R.drawable.im1));
        // Thêm các story khác
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                storyList.add(new Story(R.drawable.im1));
                if(snapshot.exists()){
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        Story story = new Story();
                        story.setStoryBy(storySnapshot.getKey());
                        story.setStoryAt(storySnapshot.child("storyAt").getValue(Long.class));
                        ArrayList<UserStories> stories = new ArrayList<>();
                        for (DataSnapshot userStorySnapshot : storySnapshot.child("userStories").getChildren()) {
                            UserStories userStory = userStorySnapshot.getValue(UserStories.class);
                            stories.add(userStory);
                        }
                        story.setStories(stories);
                        storyList.add(story);
                    }
                    storyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        dasboardRecyclerView = view.findViewById(R.id.dashboardRV);
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        dasboardRecyclerView.setLayoutManager(linearLayoutManager1);
        dasboardRecyclerView.setNestedScrollingEnabled(false);
        dasboardRecyclerView.setAdapter(postAdapter);

        database.getReference().child("Posts").addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}

        });

        // Lấy danh sách bài post được chia sẻ từ UserShares
        String currentUserId = auth.getCurrentUser().getUid();
        sharedBy = currentUserId;
        database.getReference().child("UserShares").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userid : snapshot.getChildren()) {
                        for (DataSnapshot postId : userid.getChildren()) {
                            String postIdValue = postId.getKey();
                            fetchSharedPost(postIdValue, userid.getKey());
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading shared posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchSharedPost(String postId, String sharedBy) {
        database.getReference().child("Posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    post.setShared(true);
                    post.setSharedBy(sharedBy);
                    postList.add(post);
                    dasboardRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Error fetching shared post: " + error.getMessage());
            }
        });
    }

    private void uploadImageToFirebase(Uri uri) {
        dialog.show();
        CloudinaryUtil.uploadImage(getContext(), uri, new CloudinaryUtil.UploadImageResultListener() {
            @Override
            public void onSuccess(String imageUrl) {
                Story story = new Story();
                story.setStoryAt(new Date().getTime());

                database.getReference()
                        .child("stories")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child("storyAt")
                        .setValue(new Date().getTime())
                        .addOnSuccessListener(unused -> {
                            UserStories stories = new UserStories(imageUrl, story.getStoryAt());
                            database.getReference()
                                    .child("stories")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .child("userStories")
                                    .push()
                                    .setValue(stories)
                                    .addOnSuccessListener(aVoid -> {
                                        dialog.dismiss();
                                    });
                        });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}