package com.bugbug.blogapp.Model;

import java.util.ArrayList;

public class Story {

    private int story;
    private String StoryBy;
    private long StoryAt;
    ArrayList<UserStories> stories;
    private boolean isCreateStory;

    public Story(String storyBy, long storyAt) {
        this.StoryBy = storyBy;
        this.StoryAt = storyAt;
        this.isCreateStory = false;
    }
    public Story(int story) {
        this.story = story;
        this.isCreateStory = true;
    }

    public Story() {
    }

    public int getStory() {
        return story;
    }

    public void setStory(int story) {
        this.story = story;
    }


    public boolean isCreateStory() { return isCreateStory; }

    public String getStoryBy() {
        return StoryBy;
    }

    public void setStoryBy(String storyBy) {
        StoryBy = storyBy;
    }

    public long getStoryAt() {
        return StoryAt;
    }

    public void setStoryAt(long storyAt) {
        StoryAt = storyAt;
    }

    public ArrayList<UserStories> getStories() {
        return stories;
    }

    public void setStories(ArrayList<UserStories> stories) {
        this.stories = stories;
    }
}
