package com.bugbug.blogapp.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Activity.CommentActivity;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.DasboardRvSampleBinding;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final ArrayList<Post> postList;
    private final Context context;
    private final String currentUserId;

    public PostAdapter(ArrayList<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dasboard_rv_sample, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        DasboardRvSampleBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DasboardRvSampleBinding.bind(itemView);
        }

        public void bind(Post post) {
            loadPostImage(post);
            binding.postDescription.setText(post.getPostDescription());
            String time = TimeAgo.using(post.getPostedAt());
            binding.bio.setText(time);
            binding.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("postId", post.getPostId());
                    intent.putExtra("postedBy", post.getPostedBy());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            if(post.isShared()){
                bindUserInfo(post.getSharedBy());
                binding.shareFrom.setVisibility(View.VISIBLE);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPostedBy());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            binding.shareFrom.setText("Share from " + user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

            }else {
                bindUserInfo(post.getPostedBy());
            }
            bindLikeState(post);
            bindCommentCount(post.getPostId());
            bindShareState(post);
        }

        private void loadPostImage(Post post) {
            Log.d("TAG", post.getPostId());
            if (post.getPostImages() == null || post.getPostImages().isEmpty()) {
                return;
            }
            PostImageAdapter adapter=new PostImageAdapter(context,post.getPostImages());
            binding.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            binding.imagesRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        private void bindUserInfo(String userId) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getCoverPhoto() != null && !user.getCoverPhoto().isEmpty()) {
                            Picasso.get()
                                    .load(user.getCoverPhoto())
                                    .placeholder(R.drawable.avatar_default)
                                    .into(binding.profileImage);
                        } else {
                            binding.profileImage.setImageResource(R.drawable.avatar_default);
                        }
                        binding.userName.setText(user.getName());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        private void bindLikeState(Post post) {
            DatabaseReference likesRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(post.getPostId())
                    .child("likes");

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int realTimeLikeCount = (int) snapshot.getChildrenCount();
                    binding.like.setText(realTimeLikeCount + "");

                    boolean isLiked = snapshot.hasChild(currentUserId);
                    binding.like.setCompoundDrawablesWithIntrinsicBounds(
                            isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart,
                            0, 0, 0
                    );

                    binding.like.setOnClickListener(view -> toggleLike(post, isLiked));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        private void toggleLike(Post post, boolean isLiked) {
            DatabaseReference postRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(post.getPostId());

            DatabaseReference likeRef = postRef
                    .child("likes")
                    .child(currentUserId);

            if (isLiked) {
                likeRef.removeValue();
                DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Notification")
                        .child(post.getPostedBy());
                notificationRef.orderByChild("postId")
                        .equalTo(post.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Notification notification = snapshot.getValue(Notification.class);
                                    if (notification != null && notification.getSenderId().equals(currentUserId) && notification.getActionType().equals("Like")) {
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
                notification.setSenderId(currentUserId);
                notification.setTimestamp(new Date().getTime());
                notification.setPostId(post.getPostId());
                notification.setReceiverId(post.getPostedBy());
                notification.setActionType("Like");

                FirebaseDatabase.getInstance().getReference()
                        .child("Notification")
                        .child(post.getPostedBy())
                        .push()
                        .setValue(notification);
            }
        }

        private void bindCommentCount(String postId) {
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

        private void bindShareState(Post post) {
            DatabaseReference sharesRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(post.getPostId())
                    .child("shares");

            sharesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long shareCount = snapshot.getChildrenCount();
                    binding.share.setText(shareCount +" ");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostAdapter", "Error fetching share count: " + error.getMessage());
                }
            });

            binding.share.setOnClickListener(view -> {
                sharePost(post);
            });
        }

        private void sharePost(Post post) {
            DatabaseReference userSharesRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("UserShares")
                    .child(currentUserId)
                    .child(post.getPostId());

            DatabaseReference postSharesRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(post.getPostId())
                    .child("shares")
                    .child(currentUserId);

            userSharesRef.setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        postSharesRef.setValue(true);
                        ((Activity) context).recreate();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to share post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
