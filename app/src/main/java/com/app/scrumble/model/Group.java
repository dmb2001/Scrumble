package com.app.scrumble.model;

import com.app.scrumble.model.User;

import java.util.ArrayList;
import java.util.List;

public final class Group {

    private int groupPhotoResourceID;
    private String name;
    private List<User> members = new ArrayList<>();
    private List<Scrapbook> recentPosts = new ArrayList<>();

    public Group(String name, int groupPhotoResourceID){
        this.name = name;
        this.groupPhotoResourceID = groupPhotoResourceID;
    }

    public void addMember(User member){
        this.members.add(member);
    }

    public List<User> getMembers(){
        return this.members;
    }

    public String getName() {
        return name;
    }

    public void addRecentPost(Scrapbook scrapbook){
        this.recentPosts.add(scrapbook);
    }

    public List<Scrapbook> getRecentPosts() {
        return recentPosts;
    }

    public int getGroupPhotoResourceID() {
        return groupPhotoResourceID;
    }
}
