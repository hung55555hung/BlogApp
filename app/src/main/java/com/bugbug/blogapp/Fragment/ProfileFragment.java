package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.FollowAdapter;
import com.bugbug.blogapp.Model.Follow;
import com.bugbug.blogapp.R;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<Follow> list;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile, container, false);
        recyclerView=view.findViewById(R.id.friendRV);
        list=new ArrayList<>();
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
        FollowAdapter adapter=new FollowAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return view;
    }
}