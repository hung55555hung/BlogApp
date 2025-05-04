package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Model.Follow;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.UserSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Context context;
    ArrayList<User> list;
    public UserAdapter(ArrayList<User> list, Context context) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(com.bugbug.blogapp.R.layout.user_sample, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.avt).into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());

        FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(user.getUserID()).child("followers")
                        .child(user.getUserID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.following_btn));
                            holder.binding.followBtn.setText("Following");
                            holder.binding.followBtn.setOnClickListener(v -> handleUnfollow(user));
                        } else {
                            holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_btn));
                            holder.binding.followBtn.setText("Follow");
                            holder.binding.followBtn.setOnClickListener(v -> handleFollow(user));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void handleFollow(User user){
        Follow follow=new Follow();
        follow.setFollowBy("aaaa");
        follow.setFollowAt(new Date().getTime());

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(user.getUserID())
                .child("followers")
                .child("aaa")
                .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUserID())
                                .child("numberFollower")
                                .setValue(user.getNumberFollower()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context,"You followed "+user.getName(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
    private void handleUnfollow(User user){
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(user.getUserID())
                .child("followers")
                .child("aaa")
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUserID())
                                .child("numberFollower")
                                .setValue(user.getNumberFollower()-1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context,"You unfollowed "+user.getName(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        UserSampleBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserSampleBinding.bind(itemView);
        }

    }
}
