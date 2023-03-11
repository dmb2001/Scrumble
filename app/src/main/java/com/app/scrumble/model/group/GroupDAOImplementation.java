package com.app.scrumble.model.group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.group.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupDAOImplementation implements GroupDAO{

    SQLiteDatabase database;
    ScrapbookDAO scrapbookDAO;

    UserDAO userDAO;


    //I shamelessly stole this from Robbie's UserDAOImplementation class - forgive me, Robbie!
    private String getStringFromCursor (Cursor cursor, String indexName) {
        int index = cursor.getColumnIndex(indexName);
        return cursor.getString(index);
    }

    private long getLongFromCursor (Cursor cursor, String indexName) {
        int index = cursor.getColumnIndex(indexName);
        return cursor.getLong(index);
    }

    public GroupDAOImplementation(SQLiteDatabase database, ScrapbookDAO scrapbookDAO, UserDAO userDAO) {
        this.database = database;
        this.scrapbookDAO = scrapbookDAO;
    }

    @Override
    public void createGroup(Group group) {
        //Create a ContentValues class that holds group-related values to be added to the new row
        ContentValues values = new ContentValues();

        //Put the values into it
        values.put("GroupID", group.getID());
        values.put("GroupName", group.getName());
        values.put("GroupOwnerID", group.getMembers().get(0).getId()); //I assume that, when a group is created, its first member is the
        values.put("ImageID",0);
        //"owner", since, by being the first member, it implies they're the first member.
        //For the time being, I also ignored ImageID, since that'll be implemented later, I presume

        //Insert the values into a new row in the Groups table, using the values from the vals
        database.insertOrThrow("Groups",null,values);

        //Also, make the first member of the group, i.e. the creator, join the group using the next method
        joinGroup(group.getMembers().get(0).getId(),group.getID());

    }

    @Override
    public void joinGroup(long userID, long groupID) {
        //Create ContentValues class that holds group-related values to be added to the new row
        ContentValues values = new ContentValues();

        //Put values in
        values.put("GroupID",groupID);
        values.put("UserID",userID);

        //Insert the values as a new row in the UserGroups table
        database.insertOrThrow("UserGroups",null,values);
    }

    @Override
    public void postToGroup(long scrapbookID, long groupID) {
        //Create ContentValues class that holds group-related values to be added to the new row
        ContentValues values = new ContentValues();

        //Put in values
        values.put("ScrapbookID",scrapbookID);
        values.put("GroupID",groupID);

        //Insert the values as a new row in the ScrapbookGroups table
        database.insertOrThrow("ScrapbookGroups",null,values);
    }

    @Override
    public void leaveGroup(long userID, long groupID) {
        database.delete("UserGroups","userID=? AND groupID=?", new String[]{Long.toString(userID),Long.toString(groupID)});
    }

    @Override
    public Set<Group> queryGroupsContainingScrapbookID(long scrapbookID) {
        //Run an SQLite query, looking for all columns for groups whose ID aligns with a "ScrapbookGroups" entry
        Cursor cursor = database.rawQuery(
                "SELECT GroupID FROM ScrapbookGroups" +
                        " WHERE ScrapbookID = ?;"
                , new String[]{Long.toString(scrapbookID)}
        );

        //If there are no groups associated with a scrapbook, i.e. the count of the Cursor is 0, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();

        //Initialize a hashset for all groups
        Set<Group> groups = new HashSet<Group>();

        while (cursor.moveToNext()) {
            Group nextGroup = queryGroupByID(getLongFromCursor(cursor,"GroupID"));
            groups.add(nextGroup);
        }

        return groups;
    }

    @Override
    public Group queryGroupByID(long groupID) {
        //Run an SQLite query, looking for all rows whose ID is the group ID
        Cursor cursor = database.rawQuery(
                "SELECT * FROM Groups" +
                        " WHERE GroupID = ?;"
                , new String[]{Long.toString(groupID)}
        );

        //If the cursor has 0 entries, i.e. there's no groups matching this ID, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();

        //Initialize a variable for the list of members of the group
        ArrayList<User> groupMembers = new ArrayList<User>();
        long groupOwnerID; //Initialize the long of the group owner

        //Otherwise, get the required values from the Cursor
        cursor.moveToFirst();
        String groupName = getStringFromCursor(cursor, "GroupName");
        groupOwnerID = getLongFromCursor(cursor,"GroupOwnerID");
        User groupOwner = userDAO.queryUserByID(groupOwnerID);

        //Next, get all users following the group
        groupMembers = (ArrayList<User>)queryUsersFollowingGroup(groupID);

        //Go through all the group Members, remove the element corresponding to the
        //ID of the owner of the group, and insert the Group Owner into the beginning of the ArrayList
        for (User user : groupMembers) {
            if (user.getId() == groupOwnerID) {
                groupMembers.remove(user);
            }
        }

        groupMembers.add(0,groupOwner);

        //Finally, create a Group instance and return it, using the results acquired
        return new Group(groupID,groupName,groupMembers);

    }

    @Override
    public List<User> queryUsersFollowingGroup(long groupID) {
        //Run an SQLite query, looking for all IDs in the User table whose ID is associated with the group ID
        //in the UserGroups table
        Cursor cursor = database.rawQuery(
                "SELECT UserID FROM UserGroups" +
                        " WHERE GroupID = ?;"
                , new String[]{Long.toString(groupID)}
        );

        //If there are no entries, i.e. there are no users following this group, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();

        //Create an Arraylist to hold group members
        List<User> groupMembers = new ArrayList<User>();

        while (cursor.moveToNext()) {
            User user = userDAO.queryUserByID(getLongFromCursor(cursor,"UserID"));
            groupMembers.add(user);
        }

        return groupMembers;
    }

    @Override
    public List<Scrapbook> queryScrapbooksInGroup(long groupID) {
        //Run an SQLite query, looking for all Scrapbook IDs associated with the group ID
        //in the ScrapbookGroups table
        Cursor cursor = database.rawQuery(
                "SELECT ScrapbookID FROM Scrapbooks,ScrapbookGroups" +
                        " WHERE GroupID = ?" +
                        " AND Scrapbooks.ScrapbookID = ScrapbookGroups.ScrapbookID" +
                        " ORDER BY Timestamp DESC;"
                , new String[]{Long.toString(groupID)}
        );

        //If no scrapbooks are associated with the group, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        //Next, use the ScrapbookDAO to get Scrapbooks from the group, based on their ids

        cursor.moveToFirst();

        List<Scrapbook> result = new ArrayList<Scrapbook>();

        while (cursor.moveToNext()) {
            Scrapbook scrapbook = scrapbookDAO.queryScrapbookByID(getLongFromCursor(cursor,"ScrapbookID"));
            result.add(scrapbook);
        }

        return result;
    }

    @Override
    public List<Group> queryUserGroups(long userID) {
        //Run an SQLite query, looking for all Group IDs associated with the User ID
        //in the UserGroups table
        Cursor cursor = database.rawQuery(
                "SELECT GroupID FROM UserGroups,Groups" +
                        " WHERE UserId = ?" +
                        " AND UserGroups.GroupID = Groups.GroupID" +
                        " ORDER BY GroupName ASC;"
                , new String[]{Long.toString(userID)}
        );

        //If the user follows no groups, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        List<Group> result = new ArrayList<Group>();

        cursor.moveToFirst();

        //Using the queryGroupByID method, get all the groups based on the GroupIDs acquired, and add them to the result list
        while (cursor.moveToNext()) {
            Group group = queryGroupByID(getLongFromCursor(cursor,"GroupID"));
            result.add(group);
        }

        return result;
    }
}
