package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Model.FriendModel;
import com.bugbug.blogapp.R;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    ArrayList<FriendModel> list;
    Context context;

    public FriendAdapter(ArrayList<FriendModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.friend_rv,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendModel model=list.get(position);
        holder.profile.setImageResource(model.getProfile());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profile;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            profile=itemView.findViewById(R.id.profile_image);
        }
    }
}
