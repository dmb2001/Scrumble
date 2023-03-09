package com.app.scrumble;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.app.scrumble.model.CustomDatabaseOpenHelper;
import com.app.scrumble.model.group.GroupDAO;
import com.app.scrumble.model.group.GroupDAOImplementation;
import com.app.scrumble.model.group.scrapbook.DemoScrapbookDAO;
import com.app.scrumble.model.group.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.UserDAO;
import com.app.scrumble.model.user.UserDAOImplementation;

public class Scrumble extends Application {

    private SQLiteDatabase database;

    public final SQLiteDatabase getDatabase(){
        if(database == null){
            database = new CustomDatabaseOpenHelper(getApplicationContext()).getWritableDatabase();
        }
        return database;
    }

    public final UserDAO getUserDAO(){ return new UserDAOImplementation(getDatabase());}

    public final ScrapbookDAO getScrapBookDAO(){
        return new DemoScrapbookDAO(getDatabase(), getUserDAO());
    }

    public final GroupDAO getGroupDAO(){
        return new GroupDAOImplementation(getDatabase(), getScrapBookDAO(), getUserDAO());
    }

}
