package com.bugbug.blogapp.Model;

import java.util.ArrayList;

public class Post {
    private String postId;
    private ArrayList<String> postImages;
    private String postedBy;
    private String postDescription;
    private long postedAt;

    public Post(){}

    public Post(String postId, ArrayList<String> postImages, String postedBy, String postDescription, long postedAt) {
        this.postId = postId;
        this.postImages = postImages;
        this.postedBy = postedBy;
        this.postDescription = postDescription;
        this.postedAt = postedAt;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public ArrayList<String> getPostImages() {
        return postImages;
    }

    public void setPostImages(ArrayList<String> postImages) {
        this.postImages = postImages;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public long getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(long postedAt) {
        this.postedAt = postedAt;
    }
}
