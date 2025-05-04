package com.bugbug.blogapp.Model;

public class User {
    private String userID;
    private String name;
    private String profession;
    private String email;
    private String password;
    private String coverPhoto;
    private String bio;
    private int numberFollower;
     public User() {

     }

    public User(String userID,String name, String profession, String email,String password, String coverPhoto, String bio) {
         this.userID=userID;
        this.name = name;
        this.profession = profession;
        this.password = password;
        this.email = email;
        this.coverPhoto = coverPhoto;
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getNumberFollower() {
        return numberFollower;
    }

    public void setNumberFollower(int numberFollower) {
        this.numberFollower = numberFollower;
    }
}
