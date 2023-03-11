package com.app.scrumble.model.group;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;

import java.util.List;
import java.util.Set;

public interface GroupDAO {

    //Abstract method for creating a group and puts it into the Database, based on a Group Instance given to it
    void createGroup(Group group);

    //Abstract method for a specific User ID to join a specific Group ID
    void joinGroup(long userID, long groupID);

    //Abstract method for associating a Scrapbook with a Group
    void postToGroup(long scrapbookID, long groupID);

    //Abstract method for a user to leave a group
    void leaveGroup(long userID, long groupID);

    Set<Group> queryGroupsContainingScrapbookID(long scrapbookID);

    //Abstract method from getting a group from an ID
    Group queryGroupByID(long groupID);

    //Abstract method to get the users in a group
    List<User> queryUsersFollowingGroup(long groupID);

    //Abstract method to get all scrapbooks in a group
    List<Scrapbook> queryScrapbooksInGroup(long groupID);

    //Abstract method to get all groups followed by a user
    List<Group> queryUserGroups(long userID);

}
