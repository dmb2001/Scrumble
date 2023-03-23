package com.app.scrumble.model.user;

import android.util.Log;

import com.app.scrumble.model.RemoteDatabaseConnection;
import com.app.scrumble.model.RemoteDatabaseConnection.InsertResult;
import com.app.scrumble.model.user.User.UserBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RemoteUserDAO implements UserDAO{

    private final RemoteDatabaseConnection database;

    public RemoteUserDAO(RemoteDatabaseConnection database) {
        this.database = database;
    }

    @Override
    public void create(User user) {
        InsertResult result = database.executeInsert("Users", new String[]{"Name","Username","EmailAddress","Password","TypeUser"}, new Object[]{user.getName(), user.getUsername(),user.getEmail(), user.getPassword(), "User"});
        user.setUniqueID(result.getGeneratedID());
        Log.d("DEBUGGING", "insert result: " + result.isSuccessful() + " generated key: " + result.getGeneratedID());
    }

    @Override
    public User queryUserByID(long userID) {
        List<Map<String,Object>> results = database.executeQuery("Users", null, "UserID = ?", new Object[]{userID});
        if(results.size() == 1){
            return convertToUser(results.get(0));
        }
        return null;
    }

    @Override
    public User queryUserByUsername(String username) {
        List<Map<String,Object>> results = database.executeQuery("Users", null, "Username = ?", new Object[]{username});
        if(results.size() == 1){
            return convertToUser(results.get(0));
        }
        return null;
    }

    private User convertToUser(Map<String,Object> row){
        UserBuilder builder = new UserBuilder();
        for(Entry<String,Object> entry : row.entrySet()){
            if(entry.getKey().equals("UserID")){
                builder.withID(((Integer) entry.getValue()).longValue());
            }else if(entry.getKey().equals("Name")){
                builder.withName((String) entry.getValue());
            }else if(entry.getKey().equals("EmailAddress")){
                builder.withEmail((String) entry.getValue());
            }else if(entry.getKey().equals("Username")){
                builder.withUsername((String) entry.getValue());
            }else if(entry.getKey().equals("Password")){
                builder.withPassword((String) entry.getValue());
            }else if(entry.getKey().equals("TypeUser")){
                String storedType = (String) entry.getValue();
                if(storedType.equals("User")){
                    builder.withUserType(User.TYPE_USER);
                }else {
                    builder.withUserType(User.TYPE_ADMIN);
                }
            }
        }
        return builder.build();
    }

    @Override
    public void updateUser(User user) {
        database.executeUpdate("Users", new String[]{"Name", "EmailAddress", "Password"}, new Object[]{user.getUsername(), user.getEmail(), user.getPassword()}, "UserID = ?", new Object[]{user.getId()});
    }

    @Override
    public void deleteUser(long userID) {
        database.executeDelete("Users", "UserID = ?", new Object[]{userID});
    }

    @Override
    public List<User> getFollowing(User user) {
//        TODO there's no table in the remote database for followers
//        String sql = "SELECT FollowRecords.*, Users.* " +
//                "FROM FollowRecords " +
//                "JOIN Users " +
//                "ON FollowRecords.Follower = Users.UserID " +
//                "WHERE FollowRecords.Leader = ?;";
//        List<Map<String,Object>> rows = database.executeRawQuery(sql, new Object[]{user.getId()});
//        for(Map<String,Object> row : rows){
//            UserBuilder userBuilder =
//                    new UserBuilder().withID(new Long((int)row.get("UserID")))
//                            .withName((String)row.get("Name"))
//                            .withUsername((String)row.get("Username"))
//                            .withEmail((String)row.get("EmailAddress"))
//                            .withPassword((String)row.get("Password"))
//                            .withUserType(User.TYPE_USER);
//            following.add(userBuilder.build());
//        }
        List<Map<String, Object>> rows = database.executeQuery("FollowRecords", null, "Follower = ?", new Object[]{user.getId()});
        List<User> following = new ArrayList<>();
        for(Map<String,Object> row : rows){
            following.add(queryUserByID(new Long((int)row.get("Leader"))));
        }
        return following;
    }

    @Override
    public void follow(long leaderID, long followerID) {
        database.executeInsert("FollowRecords", new String[]{"Leader", "Follower"}, new Object[]{leaderID, followerID});
    }

    @Override
    public boolean checkIfFollowing(long leaderID, long followerID) {
        List<Map<String,Object>> rows = database.executeQuery("FollowRecords", null, "Leader = ? AND Follower = ?", new Object[]{leaderID, followerID});
        return rows.size() > 0;
    }

}
