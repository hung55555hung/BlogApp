package com.bugbug.blogapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bugbug.blogapp.Adapter.CommentAdapter;
import com.bugbug.blogapp.Model.Comment;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.ActivityCommentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class CommentActivity extends AppCompatActivity {

    private ActivityCommentBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String postId;
    private String postedBy;

    private final ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        loadPostDetails();
        loadPostedByUser();
        setupCommentRecyclerView();
        loadComments();
        setupPostCommentButton();
    }

    private void initialize() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        binding.btnReturn.setOnClickListener(v->{finish();});
    }

    private void loadPostDetails() {
        database.getReference()
                .child("Posts")
                .child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        if (post == null) return;

                        Picasso.get()
                                .load(post.getPostImage())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.postImg);

                        binding.postDescription.setText(post.getPostDescription());
                        binding.like.setText(post.getPostLikes()+" Likes");
                        binding.comment.setText(post.getCommentCount()+ " Comments");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadPostedByUser() {
        database.getReference()
                .child("Users")
                .child(postedBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String profession=snapshot.child("profession").getValue(String.class);
                        String coverPhoto = snapshot.child("coverPhoto").getValue(String.class);

                        if (coverPhoto != null && !coverPhoto.isEmpty()) {
                            Picasso.get()
                                    .load(coverPhoto)
                                    .placeholder(R.drawable.avatar_default)
                                    .into(binding.profileImage);
                        } else {
                            binding.profileImage.setImageResource(R.drawable.avatar_default);
                        }

                        binding.userName.setText(name);
                        binding.professionTv.setText(profession);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupPostCommentButton() {
        binding.commentPostBtn.setOnClickListener(view -> {
            String commentText = binding.commentEt.getText().toString().trim();
            if (commentText.isEmpty()) return;

            Comment comment = new Comment();
            comment.setCommentBody(commentText);
            comment.setCommentAt(new Date().getTime());
            comment.setCommentedBy(auth.getUid());

            database.getReference()
                    .child("Posts")
                    .child(postId)
                    .child("comments")
                    .push()
                    .setValue(comment)
                    .addOnSuccessListener(unused -> updateCommentCount());

        });
    }

    private void updateCommentCount() {
        database.getReference()
                .child("Posts")
                .child(postId)
                .child("commentCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;

                        database.getReference()
                                .child("Posts")
                                .child(postId)
                                .child("commentCount")
                                .setValue(currentCount + 1)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(CommentActivity.this, "Commented", Toast.LENGTH_SHORT).show();
                                    binding.commentEt.setText("");
                                });
                        Notification notification=new Notification();
                        notification.setSenderId(auth.getCurrentUser().getUid());
                        notification.setTimestamp(new Date().getTime());
                        notification.setPostId(postId);
                        notification.setReceiverId(postedBy);
                        notification.setActionType("Comment");

                        database.getReference().child("Notification")
                                .child(postedBy)
                                .push()
                                .setValue(notification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupCommentRecyclerView() {
        commentAdapter = new CommentAdapter(this, commentList);
        binding.commentRv.setLayoutManager(new LinearLayoutManager(this));
        binding.commentRv.setAdapter(commentAdapter);
    }

    private void loadComments() {
        database.getReference()
                .child("Posts")
                .child(postId)
                .child("comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentList.clear();

                        for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                            Comment comment = commentSnapshot.getValue(Comment.class);
                            if (comment == null) continue;

                            fetchUserNameForComment(comment);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchUserNameForComment(Comment comment) {
        String userId = comment.getCommentedBy();

        database.getReference()
                .child("Users")
                .child(userId)
                .child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        comment.setComentByName(name);
                        commentList.add(comment);
                        commentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }
}
