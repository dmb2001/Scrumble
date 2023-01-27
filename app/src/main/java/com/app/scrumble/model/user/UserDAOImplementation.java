package com.app.scrumble.model.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDAOImplementation implements UserDAO{
    SQLiteDatabase database;

    UserDAOImplementation (SQLiteDatabase database) {
        this.database = database;
    }

    private String getStringFromCursor (Cursor cursor, String indexName) {
        int index = cursor.getColumnIndex(indexName);
        return cursor.getString(index);
    }

    private long getLongFromCursor (Cursor cursor, String indexName) {
        int index = cursor.getColumnIndex(indexName);
        return cursor.getLong(index);
    }

    @Override
    public void create(User user) {
        // create a ContentValues class (map) that holds the values to be added to the new row
        ContentValues vals = new ContentValues();
        vals.put("UserID", user.getId());
        vals.put("Name", user.getName());
        vals.put("Username", user.getUsername());
        vals.put("Email", user.getEmail());
        vals.put("UserType", user.getUserType());
        vals.put("Password", user.getPassword());

        // insert a new row to the users table using the values from vals
        database.insertOrThrow("Users", null, vals);
    }

    @Override
    public User queryUserByID(long userID) {
        // run sqlite query and return cursor
        Cursor cursor = database.rawQuery(
                "SELECT * FROM Users" +
                        " WHERE UserID = ?;"
                , new String[]{Long.toString(userID)}
        );

        // if cursor returns an empty query, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        // get the needed values from the cursor
        cursor.moveToFirst();
        String name = getStringFromCursor(cursor, "Name");
        String username = getStringFromCursor(cursor, "Username");
        String email = getStringFromCursor(cursor, "Email");
        long userType = getLongFromCursor(cursor, "UserType");
        String password = getStringFromCursor(cursor, "Password");

        // create a new user class from the returned values
        return new User(name, email, password, username, userID, userType);
    }

    @Override
    public User queryUserByUsername(String username) {
        // run sqlite query and return cursor
        Cursor cursor = database.rawQuery(
                "SELECT * FROM Users" +
                        " WHERE Username = ?;"
                , new String[]{username}
        );

        // if cursor returns an empty query, return null
        if (cursor.getCount() == 0) {
            return null;
        }

        // get the needed values from the cursor
        cursor.moveToFirst();
        String name = getStringFromCursor(cursor, "Name");
        long userID = getLongFromCursor(cursor, "UserID");
        String email = getStringFromCursor(cursor, "Email");
        long userType = getLongFromCursor(cursor, "UserType");
        String password = getStringFromCursor(cursor, "Password");

        // create a new user class from the returned values
        return new User(name, email, password, username, userID, userType);
    }

    @Override
    public void updateUser(User user) {
        // create a ContentValues class (map) that holds the values to be added to the new row
        ContentValues vals = new ContentValues();
        vals.put("Name", user.getName());
        vals.put("Username", user.getUsername());
        vals.put("Email", user.getEmail());
        vals.put("UserType", user.getUserType());
        vals.put("Password", user.getPassword());

        // update the correct user using the returned values
        String[] idStr = {Long.toString(user.getId())}; // gets id of user to update
        database.update("Users", vals, "UserID=?", idStr);
    }

    @Override
    public void deleteUser(long userID) {
        // delete the correct user
        String[] idStr = {Long.toString(userID)}; // gets id of user to update
        database.delete("Users", "UserID=?", idStr);
    }
}
