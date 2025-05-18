package com.bugbug.blogapp.Activity;

import static com.bugbug.blogapp.databinding.ActivityFullScreenImageBinding.inflate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Adapter.PostAdapter;
import com.bugbug.blogapp.Adapter.UserAdapter;
import com.bugbug.blogapp.Model.Follow;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.ActivityUserProfileBinding;
import com.bugbug.blogapp.databinding.DialogUnfollowBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UserProfileActivity extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    User user;
    ArrayList<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        //Load user info
        String userId=getIntent().getStringExtra("userId");
        database.getReference().child("Users")
                        .child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user=snapshot.getValue(User.class);
                        binding.tvName.setText(user.getName());
                        binding.tvFullName.setText(user.getName());
                        binding.tvProfession.setText(user.getProfession());
                        binding.tvBio.setText(user.getBio());
                        if(user.getAddress()==null ||user.getAddress().isEmpty()){
                            binding.liveIn.setText("Not update yet");
                        }else{
                            binding.liveIn.setText(user.getAddress());
                        }
                        if(user.getWorkAt()==null||user.getWorkAt().isEmpty()){
                            binding.workAt.setText("Not update yet");
                        }else{
                            binding.workAt.setText(user.getWorkAt());
                        }
                        binding.tvPostBy.setText(user.getName()+"' s Posts");


                        String coverPhoto = user.getCoverPhoto();
                        if (coverPhoto == null || coverPhoto.isEmpty()) {
                            binding.avatarImg.setImageResource(R.drawable.avatar_default);
                        } else {
                            Picasso.get()
                                    .load(coverPhoto)
                                    .placeholder(R.drawable.avatar_default)
                                    .into(binding.avatarImg);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        //Load follower
        DatabaseReference followerRef=database.getReference().child("Users")
                                .child(userId).child("followers");
        followerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long followerCount=snapshot.getChildrenCount();
                        binding.tvFollower.setText(followerCount+"");
                        if(snapshot.hasChild(currentUser.getUid())){
                            binding.followBtn.setVisibility(View.GONE);
                            binding.followingBtn.setVisibility(View.VISIBLE);
                            binding.followingBtn.setOnClickListener(v -> showUnfollowBottomSheet(user));
                        } else {
                            binding.followBtn.setVisibility(View.VISIBLE);
                            binding.followingBtn.setVisibility(View.GONE);
                            binding.followBtn.setOnClickListener(v -> handleFollow(user));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        binding.textViewFollower.setOnClickListener(v->handleClikFollower());

        //Load following
        database.getReference().child("Followings").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long followingCount=snapshot.getChildrenCount();
                binding.tvFolloing.setText(followingCount+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        binding.textViewFollowing.setOnClickListener(v->handleClickFollowing());
        //Load post by user
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.postRv.setLayoutManager(layoutManager);
        binding.postRv.setAdapter(postAdapter);

        DatabaseReference userPostsRef = database.getReference().child("UserPosts").child(userId);
        userPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                binding.tvPosts.setText(snapshot.getChildrenCount()+"");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String postId = dataSnapshot.getKey();
                    database.getReference().child("Posts").child(postId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Post post = snapshot.getValue(Post.class);
                                    postList.add(post);
                                    postAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        binding.btnReturn.setOnClickListener(v->{finish();});
    }

    private void handleFollow(User user){
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
                                        Notification notification=new Notification();
                                        notification.setSenderId(currentUser.getUid());
                                        notification.setTimestamp(new Date().getTime());
                                        notification.setReceiverId(user.getUserID());
                                        notification.setActionType("Follow");

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Notification")
                                                .child(user.getUserID())
                                                .push()
                                                .setValue(notification);
                                        binding.followBtn.setVisibility(View.GONE);
                                        binding.followingBtn.setVisibility(View.VISIBLE);
                                        binding.followingBtn.setOnClickListener(v -> showUnfollowBottomSheet(user));
                                        Toast.makeText(getApplicationContext(),"You followed "+user.getName(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
    private void showUnfollowBottomSheet(User user) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_unfollow, null);
        bottomSheetDialog.setContentView(view);
        DialogUnfollowBinding binding=DialogUnfollowBinding.bind(view);
        String coverPhoto = user.getCoverPhoto();

        if ( coverPhoto == null ||  coverPhoto.isEmpty()) {
            Picasso.get()
                    .load("https://i.pinimg.com/736x/bc/43/98/bc439871417621836a0eeea768d60944.jpg")
                    .placeholder(R.drawable.avatar_default)
                    .into(binding.profileImage);
        } else {
            Picasso.get()
                    .load(coverPhoto)
                    .placeholder(R.drawable.avatar_default)
                    .into(binding.profileImage);
        }
        binding.titleText.setText("Unfollow "+user.getName()+"?");
        binding.confirmBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            handleUnfollow(user);
        });
        binding.cancelBtn.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }
    private void handleUnfollow(User user){
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
                                        DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                                                .getReference()
                                                .child("Notification")
                                                .child(user.getUserID());
                                        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    Notification notification = snapshot.getValue(Notification.class);
                                                    if (notification != null && notification.getSenderId().equals(currentUser.getUid()) && notification.getActionType().equals("Follow")) {
                                                        snapshot.getRef().removeValue();
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                        binding.followBtn.setVisibility(View.VISIBLE);
                                        binding.followingBtn.setVisibility(View.GONE);
                                        binding.followBtn.setOnClickListener(v -> handleFollow(user));
                                        Toast.makeText(getApplicationContext(),"You unfollowed "+user.getName(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
    private void handleClikFollower(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_list_user, null);
        bottomSheetDialog.setContentView(dialogView);
        ArrayList<User> list=new ArrayList<>();
        UserAdapter userAdapter = new UserAdapter(list, this);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewFl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(userAdapter);
        database.getReference().child("Users").child(user.getUserID())
                .child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String followerId = dataSnapshot.getKey();
                        if (followerId != null) {
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(followerId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            User user = userSnapshot.getValue(User.class);
                                            if (user != null) {
                                                list.add(user);
                                                userAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        bottomSheetDialog.show();
    }
    private void handleClickFollowing(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_list_user, null);
        bottomSheetDialog.setContentView(dialogView);
        ArrayList<User> list=new ArrayList<>();
        UserAdapter userAdapter = new UserAdapter(list, this);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewFl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(userAdapter);
        FirebaseDatabase.getInstance().getReference().child("Followings")
                .child(user.getUserID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String followerId = dataSnapshot.getKey();
                                if (followerId != null) {
                                    FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child(followerId)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                    User user = userSnapshot.getValue(User.class);
                                                    if (user != null) {
                                                        list.add(user);
                                                        userAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        bottomSheetDialog.show();
    }
}