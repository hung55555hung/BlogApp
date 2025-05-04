package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.UserAdapter;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.FragmentAddBinding;
import com.bugbug.blogapp.databinding.FragmentSearchBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SearchFragment extends Fragment {
    private static final long DEBOUNCE_DELAY = 1000;

    FragmentSearchBinding binding;
    BottomNavigationView bottomNav;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        database=FirebaseDatabase.getInstance();
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        userAdapter = new UserAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView = binding.userRV;
        recyclerView.setAdapter(userAdapter);

        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword=s.toString().toLowerCase();

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (keyword.isEmpty()) {
                            list.clear();
                            userAdapter.notifyDataSetChanged();
                        } else {
                            handleSearch(keyword);
                        }
                    }
                };

                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

//        list.add(new User("John Doe", "Lorem ipsum.","","", "https://example.com/profile1.jpg","hello"));
//        list.add(new User("Jane Smith", "Lorem ipsum.","","", "https://example.com/profile2.jpg","hello"));
//        list.add(new User("Alice Johnson", "Lorem ipsum.","","", "https://example.com/profile3.jpg","hello"));
//        list.add(new User("Bob Brown", "Lorem ipsum.","","", "https://example.com/profile4.jpg","hello"));
//        list.add(new User("Charlie Davis", "Lorem ipsum.","","", "https://example.com/profile5.jpg","hello"));
//        list.add(new User("David Wilson", "Lorem ipsum.","","", "https://example.com/profile6.jpg","hello"));


        return binding.getRoot();
    }

    private void handleSearch(String keyword){
        database.getReference().child("Users")
                .orderByChild("name")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user=dataSnapshot.getValue(User.class);
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
            if (!isAdded()){
                bottomNav.setVisibility(View.VISIBLE);
                return;
            }
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();

            if (heightDiff > getResources().getDisplayMetrics().density*200) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
    }
}