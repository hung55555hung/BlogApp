package com.bugbug.blogapp.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bugbug.blogapp.Adapter.NotificationAdapter;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.databinding.FragmentNotificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class NotificationFragment extends Fragment {
    FragmentNotificationBinding binding;
    List<Notification> listToday;
    List<Notification> listEarlier;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Calendar calendar;

    public NotificationFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        listEarlier=new LinkedList<>();
        listToday=new LinkedList<>();
        NotificationAdapter adapterToday=new NotificationAdapter(listToday,getContext());
        NotificationAdapter adapterEarlier=new NotificationAdapter(listEarlier,getContext());

        LinearLayoutManager layoutToday=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutEarlier=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        binding.notificationTodayRv.setLayoutManager(layoutToday);
        binding.notificationBeforeRv.setLayoutManager(layoutEarlier);

        binding.notificationTodayRv.setAdapter(adapterToday);
        binding.notificationBeforeRv.setAdapter(adapterEarlier);

        database.getReference().child("Notification")
                .child(currentUser.getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Notification notification = snapshot.getValue(Notification.class);
                        if (notification != null) {
                            notification.setId(snapshot.getKey());
                            long startOfToday = calendar.getTimeInMillis();
                            if (startOfToday<=notification.getTimestamp()) {
                                listToday.add(0, notification);
                                adapterToday.notifyItemInserted(0);
                                binding.textViewToday.setVisibility(View.VISIBLE);
                            } else {
                                listEarlier.add(0, notification);
                                adapterEarlier.notifyItemInserted(0);
                            }
                        }
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
                    {
                        Notification updatedNotification = snapshot.getValue(Notification.class);
                        if (updatedNotification != null) {
                            updatedNotification.setId(snapshot.getKey());
                            for (int i = 0; i < listToday.size(); i++) {
                                if (listToday.get(i).getId().equals(updatedNotification.getId())) {
                                    listToday.set(i, updatedNotification);
                                    adapterToday.notifyItemChanged(i);
                                    return;
                                }
                            }
                            for (int i = 0; i < listEarlier.size(); i++) {
                                if (listEarlier.get(i).getId().equals(updatedNotification.getId())) {
                                    listEarlier.set(i, updatedNotification);
                                    adapterEarlier.notifyItemChanged(i);
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Notification removedNotification = snapshot.getValue(Notification.class);
                        if (removedNotification != null) {
                            removedNotification.setId(snapshot.getKey());
                            for (int i = 0; i < listToday.size(); i++) {
                                if (listToday.get(i).getId().equals(removedNotification.getId())) {
                                    listToday.remove(i);
                                    adapterToday.notifyItemRemoved(i);
                                    return;
                                }
                            }
                            if(listToday.size()==0){
                                binding.textViewToday.setVisibility(View.GONE);
                            }else{
                                binding.textViewToday.setVisibility(View.VISIBLE);
                            }
                            for (int i = 0; i < listEarlier.size(); i++) {
                                if (listEarlier.get(i).getId().equals(removedNotification.getId())) {
                                    listEarlier.remove(i);
                                    adapterEarlier.notifyItemRemoved(i);
                                    return;
                                }
                            }
                        }
                    }
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        return binding.getRoot();
    }
}
