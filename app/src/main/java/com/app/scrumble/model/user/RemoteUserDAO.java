package com.app.scrumble.model.user;

import android.util.Log;

import com.app.scrumble.model.RemoteDatabaseConnection;
import com.app.scrumble.model.RemoteDatabaseConnection.InsertResult;

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
        InsertResult result = database.executeInsert("Users", new String[]{"Name","EmailAddress","Password","TypeUser"}, new Object[]{user.getName(), user.getEmail(), user.getPassword(), "User"});
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
        List<Map<String,Object>> results = database.executeQuery("Users", null, "name = ?", new Object[]{username});
        if(results.size() == 1){
            return convertToUser(results.get(0));
        }
        return null;
    }

    private User convertToUser(Map<String,Object> row){
        Long id = null;
        String email = null;
        String name = null;
        String password = null;
        Long userType = null;
        for(Entry<String,Object> entry : row.entrySet()){
            if(entry.getKey().equals("UserID")){
                id = (long) entry.getValue();
            }else if(entry.getKey().equals("Name")){
                name = (String) entry.getValue();
            }else if(entry.getKey().equals("EmailAddress")){
                email = (String) entry.getValue();
            }else if(entry.getKey().equals("Password")){
                password = (String) entry.getValue();
            }else if(entry.getKey().equals("TypeUser")){
                String storedType = (String) entry.getValue();
                if(storedType.equals("User")){
                    userType = User.TYPE_USER;
                }else if(storedType.equals("Moderator")){
                    userType = User.TYPE_ADMIN;
                }else if(storedType.equals("Admin")){
                    userType = User.TYPE_ADMIN;
                }
            }
        }
        return new User("unknownName", email, password, name, id, userType);
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
        return null;
    }

}
