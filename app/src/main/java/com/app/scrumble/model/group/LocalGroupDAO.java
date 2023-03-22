package com.app.scrumble.model.group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.group.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class LocalGroupDAO implements GroupDAO{

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

    public LocalGroupDAO(SQLiteDatabase database, ScrapbookDAO scrapbookDAO, UserDAO userDAO) {
        this.database = database;
        this.scrapbookDAO = scrapbookDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void createGroup(Group group) {
        //Create a ContentValues class that holds group-related values to be added to the new row
        ContentValues values = new ContentValues();

        //Put the values into it
        values.put("GroupID", group.getID());
        values.put("GroupName", group.getName());
        values.put("GroupOwnerID", group.getMembers().get(0).getId()); //I assume that, when a group is created, its first member is the
        //"owner", since, by being the first member, it implies they're the one who created the group.
        values.put("ImageID",0);
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
    public ArrayList<Group> queryGroupsContainingScrapbookID(long scrapbookID) {
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

        //Initialize an arraylist for all groups
        ArrayList<Group> groups = new ArrayList<Group>();

        while (true) {
            Group nextGroup = queryGroupByID(getLongFromCursor(cursor,"GroupID"));
            groups.add(nextGroup);
            cursor.moveToNext();
            if (cursor.isAfterLast()) {
                break;
            }
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
            Log.d("DEBUGGING:", "No group with ID found!");
            return null;
        }

        cursor.moveToFirst();

        //Initialize a variable for the list of members of the group
        ArrayList<User> groupMembers = new ArrayList<User>();
        long groupOwnerID; //Initialize the long of the group owner

        //Get the required values from the Cursor
        String groupName = getStringFromCursor(cursor, "GroupName");
        groupOwnerID = getLongFromCursor(cursor,"GroupOwnerID");
        Log.d("DEBUGGING:", "Group Owner: "+Long.toString(groupOwnerID));
        User groupOwner = userDAO.queryUserByID(groupOwnerID);

        //Next, get all users following the group
        groupMembers = (ArrayList<User>)queryUsersFollowingGroup(groupID);

        //Go through all the group Members, remove the element corresponding to the
        //ID of the owner of the group, and insert the Group Owner into the beginning of the ArrayList
        for (int i = 0; i < groupMembers.size(); i++) {
            if (groupMembers.get(i).getId() == groupOwnerID) {
                groupMembers.remove(groupMembers.get(i));
            }
        }

        groupMembers.add(0,groupOwner);

        //Finally, create a Group instance and return it, using the results acquired
        Log.d("DEBUGGING:", "Group found!");
        return new Group(getLongFromCursor(cursor,"GroupID"),groupName,groupMembers);

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

        while (true) {
            User user = userDAO.queryUserByID(getLongFromCursor(cursor,"UserID"));
            groupMembers.add(user);
            cursor.moveToNext();
            if (cursor.isAfterLast()) {
                break;
            }
        }

        return groupMembers;
    }

    @Override
    public List<Scrapbook> queryScrapbooksInGroup(long groupID) {
        //Run an SQLite query, looking for all Scrapbook IDs associated with the group ID
        //in the ScrapbookGroups table
        Cursor cursor = database.rawQuery(
                "SELECT Scrapbooks.ScrapbookID FROM Scrapbooks,ScrapbookGroups" +
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

        while (true) {
            Scrapbook scrapbook = scrapbookDAO.queryScrapbookByID(getLongFromCursor(cursor,"ScrapbookID"));
            result.add(scrapbook);
            cursor.moveToNext();
            if (cursor.isAfterLast()) {
                break;
            }
        }

        return result;
    }

    @Override
    public ArrayList<Group> queryUserGroups(long userID) {
        //Run an SQLite query, looking for all Group IDs associated with the User ID
        //in the UserGroups table
        Cursor cursor = database.rawQuery(
                "SELECT Groups.GroupID FROM UserGroups,Groups" +
                        " WHERE UserId = ?" +
                        " AND UserGroups.GroupID = Groups.GroupID" +
                        " ORDER BY GroupName ASC;"
                , new String[]{Long.toString(userID)}
        );

        //If the user follows no groups, return null
        if (cursor.getCount() == 0) {
            Log.d("DEBUGGING:", "User follows no groups!");
            return null;
        }

        Log.d("DEBUGGING:", "User does follow "+Integer.toString(cursor.getCount())+" groups!");

        ArrayList<Group> result = new ArrayList<Group>();

        cursor.moveToFirst();

        //Using the queryGroupByID method, get all the groups based on the GroupIDs acquired, and add them to the result list
        while (true) {
            Group group = queryGroupByID(getLongFromCursor(cursor,"GroupID"));
            result.add(group);
            Log.d("DEBUGGING:", "Added group "+Long.toString(group.getID())+" to result!");
            cursor.moveToNext();
            if (cursor.isAfterLast()) {
                break;
            }
        }

        Log.d("DEBUGGING:","Returning result, with "+Integer.toString(result.size())+" groups!");
        return result;
    }
}
