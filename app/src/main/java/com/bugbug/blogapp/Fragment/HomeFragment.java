package com.bugbug.blogapp.Fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.DashboardAdapter;
import com.bugbug.blogapp.Adapter.StoryAdapter;
import com.bugbug.blogapp.Model.DasboardModel;
import com.bugbug.blogapp.Model.StoryModel;
import com.bugbug.blogapp.R;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    RecyclerView storyRecyclerView, dasboardRecyclerView;
    ArrayList<StoryModel> storyList;
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
        dasboardList = new ArrayList<>();
        dasboardList.add(new DasboardModel(R.drawable.avt, R.drawable.im1, R.drawable.ic_bookmark, "John Doe", "Lorem ipsum ", "100", "50", "20"));
        dasboardList.add(new DasboardModel(R.drawable.avt, R.drawable.im1, R.drawable.ic_bookmark, "John Doe", "Lorem ipsum ", "100", "50", "20"));
        dasboardList.add(new DasboardModel(R.drawable.avt, R.drawable.im1, R.drawable.ic_bookmark, "John Doe", "Lorem ipsum ", "100", "50", "20"));
        DashboardAdapter dashboardAdapter = new DashboardAdapter(dasboardList, getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        dasboardRecyclerView.setLayoutManager(linearLayoutManager1);
        dasboardRecyclerView.setNestedScrollingEnabled(false);
        dasboardRecyclerView.setAdapter(dashboardAdapter);
        return view;
    }
}