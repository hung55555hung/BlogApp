package com.bugbug.blogapp.Model;

public class Notification {
    private String id;
    private String postId;
    private String receiverId;
    private String senderId;
    private String actionType;
    private long timestamp;
    private boolean checkOpen =false;

    public Notification() {}

    public Notification(String id, String postId, String receiverId, String senderId, String actionType, long timestamp, boolean checkOpen) {
        this.id = id;
        this.postId = postId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.actionType = actionType;
        this.timestamp = timestamp;
        this.checkOpen = checkOpen;
    }

    // Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCheckOpen() {
        return checkOpen;
    }

    public void setCheckOpen(boolean checkOpen) {
        this.checkOpen = checkOpen;
    }
}
