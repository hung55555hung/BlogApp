package com.bugbug.blogapp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugbug.blogapp.Adapter.NotificationAdapter;
import com.bugbug.blogapp.Model.NotificationModel;
import com.bugbug.blogapp.R;

import java.util.ArrayList;

public class SubNotificationFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<NotificationModel> list;

    public SubNotificationFragment() {
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
        View view= inflater.inflate(R.layout.fragment_sub_notification, container, false);
        recyclerView=view.findViewById(R.id.notification_rv);
        list=new ArrayList<>();
        list.add(new NotificationModel(R.drawable.avt,"<b>Ethan Carter</b> liked your picture","Just now"));
        list.add(new NotificationModel(R.drawable.avt,"<b>Ethan Carter</b> liked your picture","Just now"));
        list.add(new NotificationModel(R.drawable.avt,"<b>Ethan Carter</b> liked your picture","Just now"));
        list.add(new NotificationModel(R.drawable.avt,"<b>Ethan Carter</b> liked your picture","Just now"));
        list.add(new NotificationModel(R.drawable.avt,"<b>Ethan Carter</b> liked your picture","Just now"));
        NotificationAdapter adapter=new NotificationAdapter(list,getContext());
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return view;
    }
}