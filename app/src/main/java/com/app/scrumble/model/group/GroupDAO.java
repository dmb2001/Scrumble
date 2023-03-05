package com.app.scrumble.model.group;

import java.util.Set;

public interface GroupDAO {

    //Abstract method for creating a group and puts it into the Database, based on a Group Instance given to it
    void createGroup(Group group);

    //Abstract method for a specific User ID to join a specific Group ID
    void joinGroup(long userID, long groupID);

    //Abstract method for associating a Scrapbook with a Group
    void postToGroup(long scrapbookID, long groupID);

    Set<Group> queryGroupsContainingScrapbookID(long scrapbookID);

}
