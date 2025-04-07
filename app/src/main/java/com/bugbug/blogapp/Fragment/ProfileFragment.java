package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.FileObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.FriendAdapter;
import com.bugbug.blogapp.Model.FriendModel;
import com.bugbug.blogapp.R;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<FriendModel> list;


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
        list.add(new FriendModel(R.drawable.avatar_image));
        list.add(new FriendModel(R.drawable.avatar_image));
        list.add(new FriendModel(R.drawable.avatar_image));
        list.add(new FriendModel(R.drawable.avatar_image));
        list.add(new FriendModel(R.drawable.avatar_image));
        FriendAdapter adapter=new FriendAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return view;
    }
}