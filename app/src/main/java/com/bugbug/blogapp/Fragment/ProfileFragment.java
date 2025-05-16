package com.bugbug.blogapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.bugbug.blogapp.Activity.LoginActivity;
import com.bugbug.blogapp.Adapter.PostAdapter;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference userRef;

    ArrayList<Post> postList;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user=snapshot.getValue(User.class);
                        binding.textViewName.setText(user.getName());
                        binding.textViewProfession.setText(user.getProfession());
                        binding.textViewBio.setText(user.getBio());
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

                        String coverPhoto = user.getCoverPhoto();
                        if (coverPhoto == null || coverPhoto.isEmpty()) {
                            binding.profileImage.setImageResource(R.drawable.avatar_default);
                        } else {
                            Picasso.get()
                                    .load(coverPhoto)
                                    .placeholder(R.drawable.avatar_default)
                                    .into(binding.profileImage);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        //Load follower
        userRef.child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long followerCount=snapshot.getChildrenCount();
                binding.tvFollower.setText(followerCount+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //Load following
        database.getReference().child("Followings").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long followingCount=snapshot.getChildrenCount();
                        binding.tvFolloing.setText(followingCount+"");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        //Load post by user
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, this.getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        binding.postRv.setLayoutManager(layoutManager);
        binding.postRv.setAdapter(postAdapter);

        DatabaseReference userPostsRef = database.getReference().child("UserPosts").child(auth.getUid());
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
                                    post.setPostId(dataSnapshot.getKey());
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

        binding.imageViewSetting.setOnClickListener(v -> showSettingDialog(v));

        return binding.getRoot();
    }
    private void showSettingDialog(View anchorView) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_settings, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupView.findViewById(R.id.editProfile).setOnClickListener(v -> {
            popupWindow.dismiss();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        popupView.findViewById(R.id.changePassword).setOnClickListener(v -> {
            popupWindow.dismiss();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        popupView.findViewById(R.id.logout).setOnClickListener(v -> {
            popupWindow.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


        popupWindow.setElevation(10);
        popupWindow.showAsDropDown(anchorView, -476, -50);
    }



}

