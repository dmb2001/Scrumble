package com.app.scrumble.model.group;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;

import java.util.ArrayList;
import java.util.List;

public final class Group {

    private Long groupID;
    private String name;
    private List<User> members = new ArrayList<>();
    private List<Scrapbook> recentPosts = new ArrayList<>();

    private long groupOwnerID;

    public Group(Long groupID, String name){
        this.groupID = groupID;
        this.name = name;
    }

    public void setGroupID(long ID){
        this.groupID = ID;
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

    public boolean isMember(User user) {
        for (User groupUser : getMembers()) {
            if (user.getId() == groupUser.getId()) {
                return true;
            }
        }
        return false;
    }

    public long getGroupOwnerID() {
        return groupOwnerID;
    }
}
