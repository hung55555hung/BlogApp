package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.ViewPagerAdapter;
import com.bugbug.blogapp.R;
import com.google.android.material.tabs.TabLayout;


public class NotificationFragment extends Fragment {
    ViewPager viewPager;
    TabLayout tabLayout;
    public NotificationFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_notification, container, false);
        viewPager=view.findViewById(R.id.view_page);
        viewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }
}