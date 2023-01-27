package com.app.scrumble;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.app.scrumble.model.CustomDatabaseOpenHelper;
import com.app.scrumble.model.scrapbook.DemoScrapbookDAO;
import com.app.scrumble.model.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.UserDAO;

public class Scrumble extends Application {

    private SQLiteDatabase database;

    public final SQLiteDatabase getDatabase(){
        if(database == null){
            database = new CustomDatabaseOpenHelper(getApplicationContext()).getWritableDatabase();
        }
        return database;
    }

    public final UserDAO getUserDAO(){
//        return your implementation of UserDAO, passing it the Database returned by getDatabase()
        return null;
    }

    public final ScrapbookDAO getScrapBookDAO(){
        return new DemoScrapbookDAO(database);
    }

}
