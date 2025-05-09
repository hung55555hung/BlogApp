package com.bugbug.blogapp.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bugbug.blogapp.Adapter.UserAdapter;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.FragmentSearchBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class SearchFragment extends Fragment {
    private static final long DEBOUNCE_DELAY = 600;

    FragmentSearchBinding binding;
    BottomNavigationView bottomNav;

    ArrayList<User> topUsersList = new ArrayList<>();
    UserAdapter userAdapter;
    ArrayList<User> list = new ArrayList<>();
    RecyclerView recyclerView;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    FirebaseDatabase database;

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        userAdapter = new UserAdapter(list, getContext());
        recyclerView = binding.userRV;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(userAdapter);

        loadTopUsers();

        binding.searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.textView14.setVisibility(View.GONE);
                binding.backArrow.setVisibility(View.VISIBLE);
            } else {
                binding.textView14.setVisibility(View.VISIBLE);
                binding.backArrow.setVisibility(View.GONE);
                binding.searchBar.setText("");
                list.clear();
                list.addAll(topUsersList);
                userAdapter.notifyDataSetChanged();
            }
        });

        binding.backArrow.setOnClickListener(v -> {
            binding.searchBar.clearFocus();
        });

        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (keyword.isEmpty()) {
                        list.clear();
                        list.addAll(topUsersList);
                        userAdapter.notifyDataSetChanged();
                    } else {
                        handleSearch(keyword);
                    }
                };

                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return binding.getRoot();
    }

    private void handleSearch(String keyword) {
        database.getReference().child("Users")
                .orderByChild("name")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            list.add(user);
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        list.clear();
                        userAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        View rootView = binding.getRoot();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!isAdded()) {
                bottomNav.setVisibility(View.VISIBLE);
                return;
            }
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();

            if (heightDiff > getResources().getDisplayMetrics().density * 200) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadTopUsers() {
        database.getReference().child("Users")
                .orderByChild("numberFollower")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        topUsersList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            topUsersList.add(user);
                        }
                        Collections.reverse(topUsersList);
                        list.clear();
                        list.addAll(topUsersList);
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}