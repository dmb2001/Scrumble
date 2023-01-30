package com.app.scrumble.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CustomDatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DB.db";

    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_USER_ID = "UserID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_USERNAME = "Username";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_USER_TYPE = "UserType";
    public static final String COLUMN_PASSWORD = "Password";

    public static final String COLUMN_SCRAPBOOK_ID = "ScrapbookID";
    public static final String COLUMN_LIKES = "Likes";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_TIMESTAMP = "Timestamp";
    public static final String COLUMN_LATITUDE = "Latitude";
    public static final String COLUMN_LONGITUDE = "Longitude";

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
                        COLUMN_USER_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_USERNAME + " TEXT NOT NULL," +
                        COLUMN_EMAIL + " TEXT NOT NULL," +
                        COLUMN_USER_TYPE + " INTEGER NOT NULL DEFAULT 0," +
                        COLUMN_PASSWORD + " TEXT NOT NULL"
                +")"
        );

        //Create a Table for Scrapbooks
        //I Split latitude and longitude into two floats
        //A foreign key references the Scrapbook's User who posted it
        DB.execSQL(
                "CREATE TABLE Scrapbooks (" +
                        COLUMN_SCRAPBOOK_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_USER_ID + " INTEGER NOT NULL," +
                        COLUMN_LIKES + " INTEGER DEFAULT 0," +
                        COLUMN_TITLE + " TEXT NOT NULL," +
                        COLUMN_DESCRIPTION + " TEXT," +
                        COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                        COLUMN_LATITUDE + " REAL NOT NULL," +
                        COLUMN_LONGITUDE + " REAL NOT NULL," +
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
                        "Timestamp INTEGER NOT NULL," +
                        "Caption TEXT," +
                        "FOREIGN KEY(ScrapbookID) REFERENCES Scrapbooks(ScrapbookID)," +
                        "FOREIGN KEY(ImageID) REFERENCES Images(ImageID)"
                +")"
        );

        //Create a Table for Comments
        DB.execSQL(
                "CREATE TABLE Comments (" +
                        "CommentID INTEGER PRIMARY KEY," +
                        "AuthorID INTEGER NOT NULL," +
                        "ScrapbookID INTEGER NOT NULL," +
                        "CommentText TEXT NOT NULL," +
                        "Timestamp INTEGER NOT NULL," +
                        "FOREIGN KEY(ScrapbookID) REFERENCES Scrapbooks(ScrapbookID)," +
                        "FOREIGN KEY(AuthorID) REFERENCES Users(UserID)"
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

        //Create a Table for parent-child associations of Comments
        DB.execSQL(
                "CREATE TABLE ParentChildComments (" +
                        "ParentCommentID INTEGER NOT NULL," +
                        "ChildCommentID INTEGER NOT NULL," +
                        "FOREIGN KEY(ParentCommentID) REFERENCES Comments(CommentID)," +
                        "FOREIGN KEY(ChildCommentID) REFERENCES Comments(CommentID)"
                +")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
    }

}