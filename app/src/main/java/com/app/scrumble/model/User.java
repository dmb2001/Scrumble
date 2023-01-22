package com.app.scrumble.model;

import java.util.Objects;

public class User {

    private final String username;
    private int profilePictureResourceID;

    private String email;
    private String name;
    private String password;

    public User(String name, String email, String password, String username, int profilePictureResourceID){
        this.username = username;
        this.profilePictureResourceID = profilePictureResourceID;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public int getProfilePictureResourceID(){
        return profilePictureResourceID;
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
