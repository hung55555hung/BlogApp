package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Model.Comment;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.CommentSampleBinding;
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
import java.util.HashMap;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {

    Context context;
    ArrayList<Comment> list;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String postId;

    public CommentAdapter(Context context, ArrayList<Comment> list, String postId) {
        this.context = context;
        this.list = list;
        this.postId=postId;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_sample, parent, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Comment comment = list.get(position);
        String commentId=comment.getCommentId();
        String time= TimeAgo.using(comment.getCommentAt());
        holder.binding.name.setText(comment.getCommentByName());
        holder.binding.comment.setText(comment.getCommentBody());
        holder.binding.time.setText(time + "");
        database.getReference().child("Users")
                .child(comment.getCommentedBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getCoverPhoto() != null && !user.getCoverPhoto().isEmpty()) {
                                Picasso.get()
                                        .load(user.getCoverPhoto())
                                        .placeholder(R.drawable.avatar_default)
                                        .into(holder.binding.profileImage);
                            } else {
                                holder.binding.profileImage.setImageResource(R.drawable.avatar_default);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        DatabaseReference likesRef = database.getReference()
                .child("Comments")
                .child(postId)
                .child(commentId)
                .child("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int realTimeLikeCount = (int) snapshot.getChildrenCount();
                holder.binding.like.setText(realTimeLikeCount + " Likes");

                boolean isLiked = snapshot.hasChild(auth.getUid());
                holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(
                        isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart,
                        0, 0, 0
                );
                holder.binding.like.setOnClickListener(view -> toggleLike(isLiked,comment));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void toggleLike(boolean isLiked, Comment comment) {
        DatabaseReference likeRef = database.getReference()
                .child("Comments")
                .child(postId)
                .child(comment.getCommentId())
                .child("likes")
                .child(auth.getUid());
        if (isLiked) {
            likeRef.removeValue();
            DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Notification")
                    .child(comment.getCommentedBy());
            notificationRef.orderByChild("postId")
                    .equalTo(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Notification notification = snapshot.getValue(Notification.class);
                                if (notification != null && notification.getSenderId().equals(auth.getUid()) && notification.getActionType().equals("LikeComment")) {
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
            notification.setReceiverId(comment.getCommentedBy());
            notification.setActionType("LikeComment");

            FirebaseDatabase.getInstance().getReference()
                    .child("Notification")
                    .child(comment.getCommentedBy())
                    .push()
                    .setValue(notification);
        }
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        CommentSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CommentSampleBinding.bind(itemView);
        }
    }
}
