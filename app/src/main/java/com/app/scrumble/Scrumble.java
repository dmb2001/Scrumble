package com.app.scrumble;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.app.scrumble.model.CustomDatabaseOpenHelper;
import com.app.scrumble.model.ImageUploader;
import com.app.scrumble.model.RemoteDatabaseConnection;
import com.app.scrumble.model.S3ImageUploader;
import com.app.scrumble.model.group.GroupDAO;
import com.app.scrumble.model.group.LocalGroupDAO;
import com.app.scrumble.model.group.RemoteGroupDAO;
import com.app.scrumble.model.group.scrapbook.LocalScrapbookDAO;
import com.app.scrumble.model.group.scrapbook.RemoteScrapbookDAO;
import com.app.scrumble.model.group.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.RemoteUserDAO;
import com.app.scrumble.model.user.UserDAO;
import com.app.scrumble.model.user.LocalUserDAO;

public class Scrumble extends Application {

    private final RemoteDatabaseConnection databaseConnection = new RemoteDatabaseConnection();
    private SQLiteDatabase database;

    public final SQLiteDatabase getDatabase(){
        if(database == null){
            database = new CustomDatabaseOpenHelper(getApplicationContext()).getWritableDatabase();
        }
        return database;
    }

    public final UserDAO getUserDAO(){
        return new RemoteUserDAO(databaseConnection);
    }

    public final ScrapbookDAO getScrapBookDAO(){
        return new RemoteScrapbookDAO(databaseConnection, getUserDAO());
    }

    public final GroupDAO getGroupDAO(){
        return new RemoteGroupDAO(databaseConnection, getScrapBookDAO(), getUserDAO());
    }

    /**
     * @return an implementation of {@link ImageUploader}. These are single use objects, you should request a new {@link ImageUploader} using this method any time you start a new image upload.
     */
    public final ImageUploader getImageUploader(){
        return new S3ImageUploader(getApplicationContext());
    }

}
