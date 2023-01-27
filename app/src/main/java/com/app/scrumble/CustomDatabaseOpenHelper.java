package com.app.scrumble;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CustomDatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TEST_DATABASE.db";

    private static final int DATABASE_VERSION = 1;

    static final String TABLE_NAME = "TABLE_NAME";

    public CustomDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {

        //Create a Table for Users
        //Made the User Type be 0 by default(meaning a regular user) - assume 1 and 2 can be group moderators
        //and admins respectively.
        //The password is just a var char. OBVIOUSLY, in later implementations, we'll have a hash or something instead.
        DB.execSQL(
                "CREATE TABLE Users (" +
                        "UserID INTEGER PRIMARY KEY," +
                        "Name TEXT NOT NULL," +
                        "Email TEXT NOT NULL," +
                        "UserType INTEGER NOT NULL DEFAULT 0," +
                        "Password TEXT NOT NULL"
                +")"
        );

        //Create a Table for Scrapbooks
        //I Split latitude and longitude into two floats
        //A foreign key references the Scrapbook's User who posted it
        DB.execSQL(
                "CREATE TABLE Scrapbooks (" +
                        "ScrapbookID INTEGER PRIMARY KEY," +
                        "UserID INTEGER NOT NULL," +
                        "Likes INTEGER DEFAULT 0," +
                        "Title TEXT NOT NULL," +
                        "Description TEXT," +
                        "Date TEXT NOT NULL," +
                        "Time TEXT NOT NULL," +
                        "Latitude REAL NOT NULL," +
                        "Longitude REAL NOT NULL," +
                        "FOREIGN KEY(UserID) REFERENCES Users(UserID)"
                +")"
        );

        //Create a Table for Storing Image IDs, accessed by the file system
        DB.execSQL(
                "CREATE TABLE Images (" +
                        "ImageID INTEGER PRIMARY KEY"
                        +")"
        );

        //Create a Table for Entries(individual captioned images inside a scrapbook)
        //Entries refer to the ID of the Scrapbook they are in
        DB.execSQL(
                "CREATE TABLE Entries (" +
                        "ScrapbookID INTEGER NOT NULL," +
                        "EntryID INTEGER PRIMARY KEY," +
                        "ImageID INTEGER NOT NULL," +
                        "Caption VARCHAR(50)," +
                        "FOREIGN KEY(ScrapbookID) REFERENCES Scrapbooks(ScrapbookID)," +
                        "FOREIGN KEY(ImageID) REFERENCES Images(ImageID)"
                +")"
        );

        //Create a Table for Comments
        DB.execSQL(
                "CREATE TABLE Comments (" +
                        "CommentID INTEGER PRIMARY KEY,"+
                        "ScrapbookID INTEGER NOT NULL,"+
                        "CommentText TEXT NOT NULL," +
                        "FOREIGN KEY(ScrapbookID) REFERENCES Scrapbooks(ScrapbookID)"
                +")"
        );

        //Create a Table for Groups
        DB.execSQL(
                "CREATE TABLE Groups (" +
                        "GroupID INTEGER PRIMARY KEY," +
                        "ImageID INTEGER NOT NULL," +
                        "GroupName TEXT NOT NULL," +
                        "FOREIGN KEY(ImageID) REFERENCES Images(ImageID)"
                +")"
        );

        //Create a Table for Associations between Groups and Users
        DB.execSQL(
                "CREATE TABLE UserGroups (" +
                        "GroupID INTEGER NOT NULL," +
                        "UserID INTEGER NOT NULL," +
                        "FOREIGN KEY(GroupID) REFERENCES Groups(GroupID)," +
                        "FOREIGN KEY(UserID) REFERENCES Users(UserID)"
                +")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
    }

}