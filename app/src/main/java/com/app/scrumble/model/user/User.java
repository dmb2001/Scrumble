package com.app.scrumble.model.user;

import java.util.Objects;

public class User {

    private final long id;
    private final String username;

    private String email;
    private String name;
    private String password;
    private long userType;

    public User(String name, String email, String password, String username, long id, long userType){
        this.username = username;
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.userType = userType;
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

    public long getId() { return id; }

    public long getUserType() { return userType; }

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
