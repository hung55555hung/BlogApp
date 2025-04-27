package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.UserSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Context context;
    ArrayList<User> list;
    public UserAdapter(ArrayList<User> list, Context context) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(com.bugbug.blogapp.R.layout.user_sample, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.avt).into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        UserSampleBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserSampleBinding.bind(itemView);
        }

    }
}
