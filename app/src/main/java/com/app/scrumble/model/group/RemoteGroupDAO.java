package com.app.scrumble.model.group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.scrumble.model.RemoteDatabaseConnection;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.group.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteGroupDAO implements GroupDAO {
    private final RemoteDatabaseConnection database;
    ScrapbookDAO scrapbookDAO;

    UserDAO userDAO;

    public RemoteGroupDAO(RemoteDatabaseConnection database, ScrapbookDAO scrapbookDAO, UserDAO userDAO) {
        this.database = database;
        this.scrapbookDAO = scrapbookDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void createGroup(Group group) {
        RemoteDatabaseConnection.InsertResult result = database.executeInsert(
                "Groups1", new String[]{"GroupName","GroupOwnerID"},
                new Object[]{group.getName(), group.getGroupOwnerID()});

        Log.d("DEBUGGING", "Group Insert Result: " + result.isSuccessful() + ", Generated Key: " + result.getGeneratedID());

        group.setGroupID(result.getGeneratedID());

        //Also, make the first member of the group, i.e. the creator, join the group using the next method
        joinGroup(group.getMembers().get(0).getId(), result.getGeneratedID());

        }

    @Override
    public void joinGroup(long userID, long groupID) {
        RemoteDatabaseConnection.InsertResult result = database.executeInsert(
                "UserGroups", new String[]{"GroupID","UserID"},
                new Object[]{groupID, userID});
        Log.d("DEBUGGING", "Group Join Result: " + result.isSuccessful());
    }

    @Override
    public void postToGroup(long scrapbookID, long groupID) {
        RemoteDatabaseConnection.InsertResult result = database.executeInsert(
                "ScrapbookGroups", new String[]{"ScrapbookID","GroupID"},
                new Object[]{scrapbookID, groupID});
        Log.d("DEBUGGING", "Post to Group Result: " + result.isSuccessful());
    }

    @Override
    public void leaveGroup(long userID, long groupID) {
        database.executeDelete("UserGroups", "UserID = ? AND GroupID = ?", new Object[]{userID,groupID});
    }

    @Override
    public ArrayList<Group> queryGroupsContainingScrapbookID(long scrapbookID) {
        //Get a List of Maps of Strings to objects of groups corresponding to the scrapbookID.
        List<Map<String,Object>> results = database.executeQuery("ScrapbookGroups", null, "ScrapbookID = ?", new Object[]{scrapbookID});

        //If the scrapbook is not posted to any groups, return null
        if (results.size() == 0) {
            Log.d("DEBUGGING:", "Scrapbook not posted to any groups!");
            return null;
        }

        //Initialize an arraylist for all groups associated with scrapbook
        ArrayList<Group> groups = new ArrayList<Group>();

        //Go through every entry in the results, and add the associated Group to the arraylist
        for (Map<String,Object> row : results) {
            for(Map.Entry<String,Object> entry : row.entrySet()){
                if(entry.getKey().equals("GroupID")){
                    long groupID = (long) (int) entry.getValue();
                    groups.add(queryGroupByID(groupID));
                }
            }
        }

        return groups;
    }

    @Override
    public Group queryGroupByID(long groupID) {
        //Get a List of Maps of Strings to objects of groups corresponding to the ID.
        List<Map<String,Object>> results = database.executeQuery("Groups1", null, "GroupID = ?", new Object[]{groupID});
        if(results.size() == 1){
            Log.d("DEBUGGING:", "Group found!");
            //Initialize the variables
            String groupName = null;
            ArrayList<User> groupMembers = (ArrayList<User>)queryUsersFollowingGroup(groupID);
            Long groupOwnerID = null;
            User groupOwner = null;

            Map<String,Object> groupData = results.get(0);

            for(Map.Entry<String,Object> entry : groupData.entrySet()){
                if(entry.getKey().equals("GroupID")){
                    groupID = (long) (int) entry.getValue();
                }else if(entry.getKey().equals("GroupName")){
                    groupName = (String) entry.getValue();
                }else if(entry.getKey().equals("GroupOwnerID")){
                    groupOwnerID = (long) (int) entry.getValue();
                    groupOwner = userDAO.queryUserByID(groupOwnerID);
                }
            }

            //If, somehow, the groupName is still null after checking, return null
            if (groupName == null) {
                Log.d("DEBUGGING:", "GroupName not found! Returning null!");
                return null;
            }

            //If, somehow, the groupOwnerID or groupOwner are null(i.e. there was some error),
            //return null
            if (groupOwner == null || groupOwnerID == null) {
                Log.d("DEBUGGING:", "GroupOwner not found from ID! Returning null!");
                return null;
            }

            //Go through all the group Members, remove the element corresponding to the
            //ID of the owner of the group, and insert the Group Owner into the beginning of the ArrayList
            for (int i = 0; i < groupMembers.size(); i++) {
                if (groupMembers.get(i).getId() == groupOwnerID) {
                    groupMembers.remove(groupMembers.get(i));
                }
            }

            groupMembers.add(0,groupOwner);

            return new Group(groupID,groupName,groupMembers);

        }

        //If no groups, or more than one group is found, return null.
        return null;
    }

    @Override
    public List<User> queryUsersFollowingGroup(long groupID) {
        //Get a List of Maps of Strings to objects of user groups corresponding to the ID.
        List<Map<String,Object>> results = database.executeQuery("UserGroups", null, "GroupID = ?", new Object[]{groupID});

        //If there are no entries, i.e. there are no users following this group, return null
        if (results.size() == 0) {
            return null;
        }

        //Create an Arraylist to hold group members
        List<User> groupMembers = new ArrayList<User>();

        //Go through every entry in the results, and add the User, queried from the UserDAO, to
        //the group members
        for (Map<String,Object> row : results) {
            for(Map.Entry<String,Object> entry : row.entrySet()){
                if(entry.getKey().equals("UserID")){
                    long userID = (long) (int) entry.getValue();
                    groupMembers.add(userDAO.queryUserByID(userID));
                }
            }
        }

        return groupMembers;
    }

    @Override
    public List<Scrapbook> queryScrapbooksInGroup(long groupID) {
        //Get a List of Maps of Strings to objects of scrapbooks corresponding to the groupID.
        List<Map<String,Object>> results = database.executeQuery("ScrapbookGroups", null, "GroupID = ?", new Object[]{groupID});

        //If no scrapbooks are associated with the group, return null
        if (results.size() == 0) {
            return null;
        }

        //Next, use the ScrapbookDAO to get Scrapbooks from the group, based on their ids
        List<Scrapbook> result = new ArrayList<Scrapbook>();

        //Go through every entry in the results, and add the Scrapbook, queried from the ScrapbookDAO,
        // to the group scrapbooks
        for (Map<String,Object> row : results) {
            for(Map.Entry<String,Object> entry : row.entrySet()){
                if(entry.getKey().equals("ScrapbookID")){
                    long scrapbookID = (long) entry.getValue();
                    result.add(scrapbookDAO.queryScrapbookByID(scrapbookID));
                }
            }
        }

        return result;
    }

    @Override
    public ArrayList<Group> queryUserGroups(long userID) {
        //Get a List of Maps of Strings to objects of groups corresponding to the userID.
        List<Map<String,Object>> results = database.executeQuery("UserGroups,Groups1", null, "UserID = ? AND UserGroups.GroupID = Groups1.GroupID", new Object[]{userID});

        //If the user follows no groups, return null
        if (results.size() == 0) {
            Log.d("DEBUGGING:", "User follows no groups!");
            return null;
        }

        Log.d("DEBUGGING:", "User does follow "+Integer.toString(results.size())+" groups!");

        ArrayList<Group> resultGroups = new ArrayList<Group>();

        //Using the queryGroupByID method, get all the groups based on the GroupIDs acquired, and add them to the result list
        //Do this by going through each map individually

        for (Map<String,Object> row : results) {
            for(Map.Entry<String,Object> entry : row.entrySet()){
                if(entry.getKey().equals("GroupID")){
                    long groupID = (long) (int)entry.getValue();
                    resultGroups.add(queryGroupByID(groupID));
                }
            }
        }

        Log.d("DEBUGGING:","Returning result, with "+Integer.toString(resultGroups.size())+" groups!");
        return resultGroups;
    }
}
