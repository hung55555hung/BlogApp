package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Model.StoryModel;
import com.bugbug.blogapp.R;

import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    ArrayList<StoryModel> storyList;
    Context context;

    public StoryAdapter(ArrayList<StoryModel> storyList, Context context) {
        this.storyList = storyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_rv_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryModel storyModel = storyList.get(position);
        holder.storyImage.setImageResource(storyModel.getStory());
        holder.storyTypeImage.setImageResource(storyModel.getStoryType());
        holder.profileImage.setImageResource(storyModel.getProfile());
        holder.name.setText(storyModel.getName());

        // Set click listener for the story image
        holder.storyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle story image click
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView storyImage, profileImage, storyTypeImage;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.story);
            profileImage = itemView.findViewById(R.id.profile_image);
            storyTypeImage = itemView.findViewById(R.id.storyType);
            name = itemView.findViewById(R.id.name);
        }
    }

}
