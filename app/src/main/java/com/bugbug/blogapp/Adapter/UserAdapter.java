package com.bugbug.blogapp.Adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Activity.MainActivity;
import com.bugbug.blogapp.Activity.UserProfileActivity;
import com.bugbug.blogapp.Model.Follow;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.DialogUnfollowBinding;
import com.bugbug.blogapp.databinding.UserSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public UserAdapter(ArrayList<User> list, Context context) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(com.bugbug.blogapp.R.layout.user_sample, parent, false);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        String coverPhoto = user.getCoverPhoto();
        if (coverPhoto == null || coverPhoto.isEmpty()) {
            Picasso.get()
                    .load("https://i.pinimg.com/736x/bc/43/98/bc439871417621836a0eeea768d60944.jpg")
                    .placeholder(R.drawable.avt)
                    .into(holder.binding.profileImage);
        } else {
            Picasso.get()
                    .load(coverPhoto)
                    .placeholder(R.drawable.avt)
                    .into(holder.binding.profileImage);
        }
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());

        FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(user.getUserID()).child("followers")
                        .child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.binding.followBtn.setVisibility(View.GONE);
                            holder.binding.followingBtn.setVisibility(View.VISIBLE);
                            holder.binding.followingBtn.setOnClickListener(v -> showUnfollowBottomSheet(user, holder));
                        } else {
                            holder.binding.followBtn.setVisibility(View.VISIBLE);
                            holder.binding.followingBtn.setVisibility(View.GONE);
                            holder.binding.followBtn.setOnClickListener(v -> handleFollow(user,holder));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        holder.binding.userItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, UserProfileActivity.class);
                intent.putExtra("user",user);
                context.startActivity(intent);
            }
        });
    }

    private void handleFollow(User user, ViewHolder holder){
        Follow follow=new Follow();
        follow.setFollowBy(currentUser.getUid());
        follow.setFollowAt(new Date().getTime());

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(user.getUserID())
                .child("followers")
                .child(currentUser.getUid())
                .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUserID())
                                .child("numberFollower")
                                .setValue(user.getNumberFollower()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        holder.binding.followBtn.setVisibility(View.GONE);
                                        holder.binding.followingBtn.setVisibility(View.VISIBLE);
                                        holder.binding.followingBtn.setOnClickListener(v -> showUnfollowBottomSheet(user, holder));
                                        Toast.makeText(context,"You followed "+user.getName(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
    private void showUnfollowBottomSheet(User user, ViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_unfollow, null);
        bottomSheetDialog.setContentView(view);
        DialogUnfollowBinding binding=DialogUnfollowBinding.bind(view);
        String coverPhoto = user.getCoverPhoto();

        if ( coverPhoto == null ||  coverPhoto.isEmpty()) {
            Picasso.get()
                    .load("https://i.pinimg.com/736x/bc/43/98/bc439871417621836a0eeea768d60944.jpg")
                    .placeholder(R.drawable.avt)
                    .into(binding.profileImage);
        } else {
            Picasso.get()
                    .load(coverPhoto)
                    .placeholder(R.drawable.avt)
                    .into(binding.profileImage);
        }
        binding.titleText.setText("Unfollow "+user.getName()+"?");
        binding.confirmBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            handleUnfollow(user, holder);
        });
        binding.cancelBtn.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private void handleUnfollow(User user, ViewHolder holder){
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(user.getUserID())
                .child("followers")
                .child(currentUser.getUid())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUserID())
                                .child("numberFollower")
                                .setValue(user.getNumberFollower()-1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        holder.binding.followBtn.setVisibility(View.VISIBLE);
                                        holder.binding.followingBtn.setVisibility(View.GONE);
                                        holder.binding.followBtn.setOnClickListener(v -> handleFollow(user,holder));
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
