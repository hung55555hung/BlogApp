package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.UserAdapter;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    ArrayList<User> list = new ArrayList<>();
    RecyclerView recyclerView;


    public SearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.userRV);
        list.add(new User("John Doe", "Lorem ipsum.","","", "https://example.com/profile1.jpg","hello"));
        list.add(new User("Jane Smith", "Lorem ipsum.","","", "https://example.com/profile2.jpg","hello"));
        list.add(new User("Alice Johnson", "Lorem ipsum.","","", "https://example.com/profile3.jpg","hello"));
        list.add(new User("Bob Brown", "Lorem ipsum.","","", "https://example.com/profile4.jpg","hello"));
        list.add(new User("Charlie Davis", "Lorem ipsum.","","", "https://example.com/profile5.jpg","hello"));
        list.add(new User("David Wilson", "Lorem ipsum.","","", "https://example.com/profile6.jpg","hello"));
        UserAdapter userAdapter = new UserAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(userAdapter);
        return view;
    }
}