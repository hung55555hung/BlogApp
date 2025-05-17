package com.bugbug.blogapp.Adapter;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;
import static java.security.AccessController.getContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Activity.CommentActivity;
import com.bugbug.blogapp.Activity.LoginActivity;
import com.bugbug.blogapp.Fragment.ChangePasswordFragment;
import com.bugbug.blogapp.Fragment.EditProfileFragment;
import com.bugbug.blogapp.Fragment.UpdateFragment;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.Util.CloudinaryUtil;
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
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final ArrayList<Post> postList;
    private final Context context;
    private final String currentUserId;
    ProgressDialog dialog;

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
            if(!post.getPostedBy().equals(currentUserId)){
                binding.menu.setVisibility(View.GONE);
            }
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
            binding.menu.setOnClickListener(v -> showPostOption(v,post));
            bindLikeState(post);
            bindCommentCount(post.getPostId());
            bindShareState(post);
        }
        private void showPostOption(View v,Post post){
            View popupView = LayoutInflater.from(this.itemView.getContext()).inflate(R.layout.popup_post_option, null);

            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            popupView.findViewById(R.id.editPost).setOnClickListener(vi->{
                ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, UpdateFragment.newInstance(post))
                        .addToBackStack(null)
                        .commit();
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.moveTrash).setOnClickListener(vi -> {
                removePost(post);
                popupWindow.dismiss();
            });
            popupWindow.setElevation(10);
            popupWindow.showAsDropDown(v, -476, -50);
        }
        private void removePost(Post post){
            dialog = new ProgressDialog(this.itemView.getContext());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle("Deleting post");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
            List<String> imageUrls = post.getPostImages();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                CloudinaryUtil.deleteImages(imageUrls, new CloudinaryUtil.DeleteImagesResultListener() {
                    @Override
                    public void onSuccess() {
                        deletePostFromDatabase(post);
                    }

                    @Override
                    public void onFailure(List<Exception> errors) {
                        Toast.makeText(context, "Some images failed to delete", Toast.LENGTH_SHORT).show();
                        deletePostFromDatabase(post);
                    }
                });
            } else {
                deletePostFromDatabase(post);
            }
        }
        private void deletePostFromDatabase(Post post) {
            int currentPosition = getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            DatabaseReference postRef = FirebaseDatabase.getInstance()
                    .getReference("Posts")
                    .child(post.getPostId());
            postRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    postList.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    FirebaseDatabase.getInstance()
                            .getReference("UserPosts")
                            .child(currentUserId)
                            .child(post.getPostId()).removeValue();
                    dialog.dismiss();
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void loadPostImage(Post post) {
            if(post.getPostImages()==null|| post.getPostImages().isEmpty()){
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
                    .child("Comments")
                    .child(postId);
            commentCountRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long commentCount = snapshot.getChildrenCount();
                    binding.comment.setText(commentCount + " Comments");
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        private void bindShareState(Post post) {
            DatabaseReference sharesRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Posts")
                    .child(post.getPostId())
                    .child("shares");

            sharesRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                new AlertDialog.Builder(context)
                        .setTitle("Share Post")
                        .setMessage("Are you sure you want to share this post?")
                        .setPositiveButton("Share", (dialog, which) -> {
                            sharePost(post);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
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
