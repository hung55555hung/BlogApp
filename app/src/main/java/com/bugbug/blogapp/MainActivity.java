package com.bugbug.blogapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bugbug.blogapp.Fragment.AddFragment;
import com.bugbug.blogapp.Fragment.HomeFragment;
import com.bugbug.blogapp.Fragment.NotificationFragment;
import com.bugbug.blogapp.Fragment.ProfileFragment;
import com.bugbug.blogapp.Fragment.SearchFragment;
import com.bugbug.blogapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        View navIndicator = binding.navIndicator;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int index = 0;
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                index=0;
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_notification) {
                index=3;
                selectedFragment = new NotificationFragment();
            } else if (item.getItemId() == R.id.nav_add) {
                index=2;
                selectedFragment = new AddFragment();
            } else if (item.getItemId() == R.id.nav_search) {
                index=1;
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                index=4;
                selectedFragment = new ProfileFragment();
            }

            int itemWidth = bottomNavigationView.getWidth() / 5;
            float targetX = itemWidth * index + (itemWidth - navIndicator.getWidth()) / 2f;

            navIndicator.animate()
                    .translationX(targetX)
                    .setDuration(200)
                    .start();
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            bottomNavigationView.setVisibility(View.VISIBLE);
            return true;
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}