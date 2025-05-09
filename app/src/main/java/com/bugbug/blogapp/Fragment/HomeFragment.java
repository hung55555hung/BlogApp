package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.PostAdapter;
import com.bugbug.blogapp.Adapter.StoryAdapter;
import com.bugbug.blogapp.Model.DasboardModel;
import com.bugbug.blogapp.Model.Post;
import com.bugbug.blogapp.Model.StoryModel;
import com.bugbug.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;


public class HomeFragment extends Fragment {

    RecyclerView storyRecyclerView, dasboardRecyclerView;
    ArrayList<StoryModel> storyList;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;

    ArrayList<DasboardModel> dasboardList;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        storyRecyclerView = view.findViewById(R.id.storyRV);
        storyList = new ArrayList<>();
        storyList.add(new StoryModel(R.drawable.im1, R.drawable.ic_live, R.drawable.avt, "John Doe"));
        storyList.add(new StoryModel(R.drawable.avatar_default, R.drawable.ic_live, R.drawable.avt, "John Doe"));
        storyList.add(new StoryModel(R.drawable.avt, R.drawable.ic_live, R.drawable.avt, "John Doe"));
        StoryAdapter storyAdapter = new StoryAdapter(storyList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyRecyclerView.setLayoutManager(linearLayoutManager);
        storyRecyclerView.setNestedScrollingEnabled(false);
        storyRecyclerView.setAdapter(storyAdapter);


        dasboardRecyclerView = view.findViewById(R.id.dashboardRV);
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        dasboardRecyclerView.setLayoutManager(linearLayoutManager1);
        dasboardRecyclerView.setNestedScrollingEnabled(false);
        dasboardRecyclerView.setAdapter(postAdapter);

        database.getReference().child("Posts").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        return view;
    }
}