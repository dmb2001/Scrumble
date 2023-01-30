package com.app.scrumble.model.scrapbook;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.scrumble.model.CustomDatabaseOpenHelper;
import com.app.scrumble.model.scrapbook.Scrapbook.ScrapBookBuilder;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



//TODO for Tikhon: When Robbie finishes implementing UserDAO, update corresponding functions in DemoScrapbookDAO
//Todo-2 for Tikhon: At some point, create validation mechanisms for the updating and removing methods

public class DemoScrapbookDAO implements ScrapbookDAO{

    private final SQLiteDatabase database;
    private final Scrapbook.ScrapBookBuilder builder = new Scrapbook.ScrapBookBuilder();

    private UserDAO userDAO;

    //Constructor which takes an SQLite Database
    public DemoScrapbookDAO(SQLiteDatabase newDataBase, UserDAO userDAO) {
        database = newDataBase;
        this.userDAO = userDAO;
    }

    private Comment queryCommentByID(long id) {
        Cursor c = database.rawQuery("SELECT * FROM Comments WHERE CommentID=?",new String[]{Long.toString(id)});

        if (c.getCount() == 0) {
            return null;
        }

        c.close();

        long timeStamp = c.getLong(4);
        String commentText = c.getString(3);

        //Get the author from the UserDAO
        User author = userDAO.queryUserByID(c.getLong(1));

        //Now query the ParentChildComments Table for any children of this comment (recursively)
        Cursor c2 = database.rawQuery("SELECT * FROM ParentChildComments WHERE ParentCommentID=?",new String[]{Long.toString(id)});
        if (c2.getCount() == 0) {
            //If there are no child comments, return the comment with the constructor without children
             return new Comment(id,timeStamp,commentText,author);
        } else {
            //Otherwise, create a list, and recursively call this function on all child comments
            List<Comment> childrenComments = new ArrayList<>();
            while (c2.moveToNext()) {
                childrenComments.add(queryCommentByID(c2.getLong(1)));
            }
            return new Comment(id,timeStamp,commentText,author,childrenComments);
        }

    }

    @Override
    public Scrapbook queryScrapbookByID(long id) {
        //Get a query based on ID
        Cursor c = database.rawQuery("SELECT * FROM Scrapbooks WHERE ScrapbookID=?",new String[]{Long.toString(id)});

        //If the number of returned rows is 0, i.e. no Scrapbook with such an ID exists, return null
        if (c.getCount() == 0) {
            return null;
        }else{
            c.moveToFirst();
        }

        //Get the author by using the UserDAO implementation to query a User by ID
        User author = userDAO.queryUserByID(c.getLong(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_USER_ID)));

        //Get the likes from the result
        long likes = c.getLong(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_LIKES));

        //Get the title, description and timestamp of the Scrapbook from the result
        String title = c.getString(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_DESCRIPTION));
        long timeStamp = c.getLong(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_TIMESTAMP));

        //Get the location from the latitude and longitude columns of the result
        Location location = new Location(c.getDouble(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_LATITUDE)),c.getDouble(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_LONGITUDE)));

        //Close the Cursor
        c.close();

        //Get the Comments
        List<Comment> comments = new ArrayList<>();

        //Go through every comment in the database with this scrapbook's id as its ID, and use the helper function to
        //add the comment to the list of comments of the scrapbook - if the comment exists
        Cursor c2 = database.rawQuery("SELECT CommentID FROM Comments WHERE ScrapbookID=?",new String[]{Long.toString(id)});
        while (c2.moveToNext()) {
            Comment nextComment = queryCommentByID(c2.getLong(0));
            if (nextComment != null) {
                comments.add(nextComment);
            }
        }

        //Return the built scrapbook
        return builder.withID(id).withOwner(author).withTitle(title).withDescription(description)
                .withTimeStamp(timeStamp).withLocation(location).withComments(comments).build();
    }

    @Override
    public Set<Scrapbook> queryScrapbooksByLocation(Location start, long maxDistance) {
        Log.d("DEBUGGING", "querying for scrapbooks within " + maxDistance);
        Set<Scrapbook> result = new HashSet<>();

        Cursor c = database.rawQuery("SELECT * FROM Scrapbooks",new String[]{});
        Log.d("DEBUGGING", "There are: " + c.getCount() + " total scrapbooks");
        if(c.getCount() == 0){
            return null;
        }else{
//            c.moveToFirst();
            while (c.moveToNext()){
                Location location = new Location(c.getDouble(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_LATITUDE)),c.getDouble(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_LONGITUDE)));
                Log.d("DEBUGGING", "distance from stored Scrapbook: " + Location.distanceBetween(start, location));
                if(Location.distanceBetween(start, location) <= maxDistance){
                    result.add(
                            new ScrapBookBuilder()
                                    .withID(c.getLong(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_SCRAPBOOK_ID)))
                                    .withOwner(userDAO.queryUserByID(c.getLong(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_USER_ID))))
                                    .withLocation(location)
                                    .withTitle(c.getString(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_TITLE)))
                                    .withDescription(c.getString(c.getColumnIndex(CustomDatabaseOpenHelper.COLUMN_DESCRIPTION)))
                                    .build()
                    );
                }
            }
        }
        //Return the resulting hashset
        if(result.size() > 0){
            return result;
        }else{
            return null;
        }
    }

    @Override
    public Set<Scrapbook> queryScrapbooksByUser(User user) {
        Set<Scrapbook> result = new HashSet<>();

        //Get all scrapbook IDs from scrapbooks whose user ID matches with that of the user in the method
        Cursor c = database.rawQuery("SELECT ScrapbookID FROM Scrapbooks WHERE UserID=?",new String[]{Long.toString(user.getId())});
        while (c.moveToNext()) {
            Scrapbook nextBook = queryScrapbookByID(c.getLong(0));
            if (nextBook != null) {
                result.add(nextBook);
            }
        }

        return result;
    }

    @Override
    public void createScrapbook(Scrapbook scrapbook) {
        ContentValues scrapbookValues = new ContentValues();
        scrapbookValues.put("ScrapbookID",scrapbook.getID());
        scrapbookValues.put("UserID",scrapbook.getOwner().getId());
        scrapbookValues.put("Likes",scrapbook.getLikes());
        scrapbookValues.put("Title",scrapbook.getTitle());
        scrapbookValues.put("Description",scrapbook.getDescription());
        scrapbookValues.put("Timestamp",scrapbook.getTimestamp());
        scrapbookValues.put("Latitude",scrapbook.getLocation().getLatitude());
        scrapbookValues.put("Longitude",scrapbook.getLocation().getLongitude());
        database.insert("Scrapbooks",null,scrapbookValues);
    }

    @Override
    public void deleteScrapbook(long id) {
        database.delete("Scrapbooks","ScrapbookID=?",new String[]{Long.toString(id)});
    }

    @Override
    public void createEntry(Entry entry, long scrapbookID) {
        ContentValues entryValues = new ContentValues();
        entryValues.put("ScrapbookID",scrapbookID);
        entryValues.put("EntryID",entry.getID());
        entryValues.put("Timestamp",entry.getTimeStamp());
        entryValues.put("Caption",entry.getCaption());
        database.insert("Entries",null,entryValues);
    }

    @Override
    public void createComment(Comment comment, long scrapbookID) {
        ContentValues commentValues = new ContentValues();
        commentValues.put("CommentID",comment.getID());
        commentValues.put("AuthorID",comment.getAuthor().getId());
        commentValues.put("ScrapbookID",scrapbookID);
        commentValues.put("CommentText",comment.getContent());
        commentValues.put("Timestamp",comment.getTimeStamp());
        database.insert("Comments",null,commentValues);
    }
}
