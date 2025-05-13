package com.bugbug.blogapp.Model;

import java.io.Serializable;

public class User implements Serializable {
    private String userID;
    private String name;
    private String birthday;
    private String address;
    private String profession;
    private String workAt;
    private String bio;
    private String coverPhoto;
    private String email;
    private int numberFollower;
    public User() {}

    public User(String userID, String name, String birthday, String address, String profession, String workAt, String bio, String coverPhoto, String email, int numberFollower) {
        this.userID = userID;
        this.name = name;
        this.birthday = birthday;
        this.address = address;
        this.profession = profession;
        this.workAt = workAt;
        this.bio = bio;
        this.coverPhoto = coverPhoto;
        this.email = email;
        this.numberFollower = numberFollower;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getWorkAt() {
        return workAt;
    }

    public void setWorkAt(String workAt) {
        this.workAt = workAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumberFollower() {
        return numberFollower;
    }

    public void setNumberFollower(int numberFollower) {
        this.numberFollower = numberFollower;
    }
}
