package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Activity.CommentActivity;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.DasboardRvSampleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
            bindUserInfo(post.getPostedBy());
            bindLikeState(post);
            bindCommentCount(post.getPostId());
        }

        private void loadPostImage(Post post) {
            Picasso.get()
                    .load(post.getPostImage())
                    .placeholder(R.drawable.placeholder)
                    .into(binding.postImg);
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
                        binding.bio.setText(user.getProfession());
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
                    binding.like.setText((realTimeLikeCount + post.getPostLikes()) + "");

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
            DatabaseReference likeRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(post.getPostId())
                    .child("likes")
                    .child(currentUserId);

            if (isLiked) {
                likeRef.removeValue();
            } else {
                likeRef.setValue(true);
            }
        }

        private void bindCommentCount(String postId) {
            DatabaseReference commentCountRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(postId)
                    .child("commentCount");

            commentCountRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long commentCount = snapshot.getValue(Long.class);
                    if (commentCount != null) {
                        binding.comment.setText(commentCount + " Comments");
                    } else {
                        binding.comment.setText("0 Comments");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
