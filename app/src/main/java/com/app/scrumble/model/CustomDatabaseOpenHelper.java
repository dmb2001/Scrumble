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

    public static final String COLUMN_ENTRY_ID = "EntryID";
    public static final String COLUMN_IMAGE_ID = "ImageID";
    public static final String COLUMN_CAPTION = "Caption";

    public static final String COLUMN_COMMENT_ID = "CommentID";
    public static final String COLUMN_COMMENT_TEXT = "CommentText";
    public static final String COLUMN_PARENT_COMMENT_ID = "ParentCommentID";

    public static final String COLUMN_GROUP_ID = "GroupID";
    public static final String COLUMN_GROUP_NAME = "GroupName";

    public static final String COLUMN_TAG_NAME = "TagName";
    public static final String COLUMN_TAG_HIDDEN = "Hidden";

    public static final String COLUMN_GROUP_OWNER_ID = "GroupOwnerID";

    public CustomDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {

        //Create a Table for Users
        //Made the User Type be 0 by default(meaning a regular user) - assume 1 is an admin
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
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES Users(" + COLUMN_USER_ID + ")"
                +")"
        );

        //Create a Table for Storing Image IDs, accessed by the file system
        DB.execSQL(
                "CREATE TABLE Images (" +
                        COLUMN_IMAGE_ID + " INTEGER PRIMARY KEY"
                        +")"
        );

        //Create a Table for Entries(individual captioned images inside a scrapbook)
        //Entries refer to the ID of the Scrapbook they are in
        DB.execSQL(
                "CREATE TABLE Entries (" +
                        COLUMN_SCRAPBOOK_ID + " INTEGER NOT NULL," +
                        COLUMN_ENTRY_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_IMAGE_ID + " INTEGER," +
                        COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                        COLUMN_CAPTION + " TEXT," +
                        "FOREIGN KEY(" + COLUMN_SCRAPBOOK_ID + ") REFERENCES Scrapbooks(" + COLUMN_SCRAPBOOK_ID + ")," +
                        "FOREIGN KEY(" + COLUMN_IMAGE_ID + ") REFERENCES Images(" + COLUMN_IMAGE_ID + ")"
                +")"
        );

        //Create a Table for Comments
        DB.execSQL(
                "CREATE TABLE Comments (" +
                        COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        COLUMN_SCRAPBOOK_ID + " INTEGER NOT NULL, " +
                        COLUMN_COMMENT_TEXT + " TEXT NOT NULL, " +
                        COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                        COLUMN_PARENT_COMMENT_ID + " INTEGER, " +
                        "FOREIGN KEY(" + COLUMN_SCRAPBOOK_ID + ") REFERENCES Scrapbooks(" + COLUMN_SCRAPBOOK_ID + ")," +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES Users(" + COLUMN_USER_ID + ")"
                +")"
        );

        //Create a Table for Groups
        DB.execSQL(
                "CREATE TABLE Groups (" +
                        COLUMN_GROUP_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_IMAGE_ID + " INTEGER NOT NULL," +
                        COLUMN_GROUP_NAME + " TEXT NOT NULL," +
                        COLUMN_GROUP_OWNER_ID +" INTEGER NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_IMAGE_ID + ") REFERENCES Images(" + COLUMN_IMAGE_ID + ")," +
                        "FOREIGN KEY(" + COLUMN_GROUP_OWNER_ID +") REFERENCES Users(" + COLUMN_USER_ID +")"
                +")"
        );

        //Create a Table for Associations between Groups and Users
        DB.execSQL(
                "CREATE TABLE UserGroups (" +
                        COLUMN_GROUP_ID + " INTEGER NOT NULL," +
                        COLUMN_USER_ID + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_GROUP_ID + ") REFERENCES Groups(" + COLUMN_GROUP_ID + ")," +
                        "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES Users(" + COLUMN_USER_ID + ")"
                +")"
        );

        //Create a Table for Associations between Scrapbooks and Groups
        DB.execSQL(
                "CREATE TABLE ScrapbookGroups (" +
                        COLUMN_SCRAPBOOK_ID + " INTEGER NOT NULL," +
                        COLUMN_GROUP_ID + " INTEGER NOT NULL," +
                        "FOREIGN KEY (" + COLUMN_SCRAPBOOK_ID + ") REFERENCES Scrapbooks(" + COLUMN_SCRAPBOOK_ID +")," +
                        "FOREIGN KEY (" + COLUMN_GROUP_ID + ") REFERENCES Groups(" + COLUMN_GROUP_ID + ")"
                        +")"
        );

        //Create a Table to list the Tags a Scrapbook contains
        DB.execSQL(
                "CREATE TABLE ScrapbookTags (" +
                        COLUMN_SCRAPBOOK_ID + " INTEGER NOT NULL," +
                        COLUMN_TAG_NAME + " TEXT NOT NULL," +
                        "FOREIGN KEY(" + COLUMN_SCRAPBOOK_ID + ") REFERENCES Scrapbooks(" + COLUMN_SCRAPBOOK_ID + ")," +
                        "FOREIGN KEY(" + COLUMN_TAG_NAME + ") REFERENCES Tags(" + COLUMN_TAG_NAME + ")," +
                        "PRIMARY KEY(" + COLUMN_SCRAPBOOK_ID + ", " + COLUMN_TAG_NAME + ")" +
                ")"
        );

        //Create a Table for Tags
        DB.execSQL(
                "CREATE TABLE Tags (" +
                        COLUMN_TAG_NAME + " TEXT PRIMARY KEY NOT NULL," +
                        COLUMN_TAG_HIDDEN + " INTEGER " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
    }

}