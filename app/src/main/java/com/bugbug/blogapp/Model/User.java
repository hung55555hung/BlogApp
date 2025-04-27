package com.bugbug.blogapp.Model;

public class User {
    private String name;
    private String profession;
    private String email;
    private String password;
    private String coverPhoto;
    private String profile;
     public User() {

     }

    public User(String name, String profession, String password, String email, String coverPhoto, String profile) {
        this.name = name;
        this.profession = profession;
        this.password = password;
        this.email = email;
        this.coverPhoto = coverPhoto;
        this.profile = profile;
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

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
