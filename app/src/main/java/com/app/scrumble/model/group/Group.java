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

    private long groupOwnerID;

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
        this.groupOwnerID = members.get(0).getId();
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

    public void addMember(User member) {
        members.add(member);
    }

    public boolean isMember(User user) {
        for (User groupUser : getMembers()) {
            if (user.getId() == groupUser.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMember(long userID) {
        for (User groupUser : getMembers()) {
            if (groupUser.getId() == userID) {
                return true;
            }
        }
        return false;
    }

    public long getGroupOwnerID() {
        return groupOwnerID;
    }
}
