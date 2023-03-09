package com.app.scrumble.model.group;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;

import java.util.ArrayList;
import java.util.List;

public final class Group {

    private long groupID;
    private String name;
    private List<User> members = new ArrayList<>();
    private List<Scrapbook> recentPosts = new ArrayList<>();

    public Group(long groupID, String name){
        this.groupID = groupID;
        this.name = name;
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(long groupID, String name, List<User> members) {
        this.groupID = groupID;
        this.name = name;
        this.members = members;
    }

    public List<User> getMembers(){
        return this.members;
    }

    public String getName() {
        return name;
    }

    public List<Scrapbook> getRecentPosts() {
        return recentPosts;
    }

    public long getID() { return this.groupID; }
}
