package com.app.scrumble.model.user;

import java.util.Objects;

public class User {

    public static final long TYPE_USER = 0;
    public static final long TYPE_ADMIN = 1;

    private Long id;
    private String username;

    private String email;
    private String name;
    private String password;
    private Long userType;

    private User(){
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

    public static final class UserBuilder{

        private User user;

        public UserBuilder(){
            this.user = new User();
        }

        public UserBuilder withID(long id){
            this.user.id = id;
            return this;
        }

        public UserBuilder withUsername(String username){
            this.user.username = username;
            return this;
        }

        public UserBuilder withEmail(String email){
            this.user.email = email;
            return this;
        }

        public UserBuilder withName(String name){
            this.user.name = name;
            return this;
        }

        public UserBuilder withPassword(String password){
            this.user.password = password;
            return this;
        }

        public UserBuilder withUserType(long userType){
            this.user.userType = userType;
            return this;
        }

        public User build(){
            if(this.user.userType != TYPE_USER && this.user.userType != TYPE_ADMIN){
                throw new IllegalStateException("provided usertype must be one of the allowed values");
            }
            User constructedUser = this.user;
            this.user = new User();
            return constructedUser;
        }

    }

}
