package com.bugbug.blogapp.Model;

public class Comment {
    private String commentBody;
    private long commentAt;
    private String commentedBy;
    private String comentByName;

    public Comment() {
    }

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public long getCommentAt() {
        return commentAt;
    }

    public void setCommentAt(long commentAt) {
        this.commentAt = commentAt;
    }

    public String getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }

    public String getComentByName() {
        return comentByName;
    }

    public void setComentByName(String comentByName) {
        this.comentByName = comentByName;
    }
}
