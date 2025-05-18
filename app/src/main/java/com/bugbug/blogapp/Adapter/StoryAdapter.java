package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bugbug.blogapp.Model.Story;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.Model.UserStories;
import com.bugbug.blogapp.R;
import com.bugbug.blogapp.databinding.StoryRvDesignBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CREATE_STORY = 0;
    private static final int TYPE_STORY = 1;

    ArrayList<Story> storyList;
    Context context;
    OnCreateStoryClickListener listener;

    public interface OnCreateStoryClickListener {
        void onCreateStoryClicked();
    }

    public StoryAdapter(ArrayList<Story> storyList, Context context, OnCreateStoryClickListener listener) {
        this.storyList = storyList;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return storyList.get(position).isCreateStory() ? TYPE_CREATE_STORY : TYPE_STORY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CREATE_STORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.story_create_design, parent, false);
            return new CreateStoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.story_rv_design, parent, false);
            return new StoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Story story = storyList.get(position);
        if (holder instanceof CreateStoryViewHolder) {
            CreateStoryViewHolder createHolder = (CreateStoryViewHolder) holder;
            createHolder.addIcon.setVisibility(View.VISIBLE);
            createHolder.textView.setText("Create a Story");
        } else {
            StoryViewHolder storyHolder = (StoryViewHolder) holder;
            ArrayList<UserStories> stories = story.getStories();
            if (stories == null || stories.isEmpty()) {
                return;
            }
            UserStories latestStory = stories.get(stories.size() - 1);
            Picasso.get()
                    .load(latestStory.getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(storyHolder.binding.story);

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(story.getStoryBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                String coverPhoto = user.getCoverPhoto();
                                if (coverPhoto == null || coverPhoto.isEmpty()) {
                                    storyHolder.binding.profileImage.setImageResource(R.drawable.avatar_default);
                                } else {
                                    Picasso.get()
                                            .load(coverPhoto)
                                            .placeholder(R.drawable.avatar_default)
                                            .into(storyHolder.binding.profileImage);
                                }
                                storyHolder.binding.name.setText(user.getName());
                                storyHolder.binding.story.setOnClickListener(v -> {
                                    ArrayList<MyStory> myStories = new ArrayList<>();
                                    for (UserStories userStory : stories) {
                                        myStories.add(new MyStory(userStory.getImage()));
                                    }
                                    new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                                            .setStoriesList(myStories)
                                            .setStoryDuration(5000)
                                            .setTitleText(user.getName())
                                            .setSubtitleText("")
                                            .setTitleLogoUrl(user.getCoverPhoto())
                                            .setStoryClickListeners(new StoryClickListeners() {
                                                @Override
                                                public void onDescriptionClickListener(int position) {
                                                }

                                                @Override
                                                public void onTitleIconClickListener(int position) {
                                                }
                                            })
                                            .build()
                                            .show();
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi tải thông tin user", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class CreateStoryViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView storyImage;
        ImageView addIcon;
        TextView textView;

        public CreateStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.addStory);
            addIcon = itemView.findViewById(R.id.btn_create_story);
            textView = itemView.findViewById(R.id.textView2);
            storyImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCreateStoryClicked();
                }
            });
        }
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {
        StoryRvDesignBinding binding;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = StoryRvDesignBinding.bind(itemView);
        }
    }
}
