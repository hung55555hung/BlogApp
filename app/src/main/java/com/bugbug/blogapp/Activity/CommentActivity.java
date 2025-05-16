package com.bugbug.blogapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bugbug.blogapp.Adapter.CommentAdapter;
import com.bugbug.blogapp.Adapter.PostImageAdapter;
import com.bugbug.blogapp.Model.Comment;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.ActivityCommentBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
        bindCommentCount();
        bindLikeState();
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        if (post == null) return;

                        PostImageAdapter adapter=new PostImageAdapter(CommentActivity.this,post.getPostImages());
                        binding.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(CommentActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        binding.imagesRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        binding.postDescription.setText(post.getPostDescription());
                        String time = TimeAgo.using(post.getPostedAt());
                        binding.time.setText(time);
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
                    .addOnSuccessListener(un->{
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
                        binding.commentEt.setText("");
                        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    });
        });
    }

    private void bindCommentCount() {
        DatabaseReference commentCountRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Posts")
                .child(postId)
                .child("comments");

        commentCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                binding.comment.setText(commentCount + " Comments");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
    private void bindLikeState() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Posts")
                .child(postId)
                .child("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int realTimeLikeCount = (int) snapshot.getChildrenCount();
                binding.like.setText(realTimeLikeCount + "");

                boolean isLiked = snapshot.hasChild(auth.getUid());
                binding.like.setCompoundDrawablesWithIntrinsicBounds(
                        isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart,
                        0, 0, 0
                );

                binding.like.setOnClickListener(view -> toggleLike(isLiked));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void toggleLike(boolean isLiked) {
        DatabaseReference postRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Posts")
                .child(postId);

        DatabaseReference likeRef = postRef
                .child("likes")
                .child(auth.getUid());

        if (isLiked) {
            likeRef.removeValue();
            DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Notification")
                    .child(postedBy);
            notificationRef.orderByChild("postId")
                    .equalTo(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Notification notification = snapshot.getValue(Notification.class);
                                if (notification != null && notification.getSenderId().equals(auth.getUid()) && notification.getActionType().equals("Like")) {
                                    snapshot.getRef().removeValue();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        } else {
            likeRef.setValue(true);
            Notification notification=new Notification();
            notification.setSenderId(auth.getUid());
            notification.setTimestamp(new Date().getTime());
            notification.setPostId(postId);
            notification.setReceiverId(postedBy);
            notification.setActionType("Like");

            FirebaseDatabase.getInstance().getReference()
                    .child("Notification")
                    .child(postedBy)
                    .push()
                    .setValue(notification);
        }
    }
}
