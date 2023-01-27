package com.app.scrumble.model.scrapbook;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.scrumble.model.user.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO for Tikhon: When Robbie finishes implementing UserDAO, update corresponding functions in DemoScrapbookDAO
//Todo-2 for Tikhon: At some point, create validation mechanisms for the updating and removing methods

public class DemoScrapbookDAO implements ScrapbookDAO{

    private SQLiteDatabase database;
    private Scrapbook.ScrapBookBuilder builder = new Scrapbook.ScrapBookBuilder();

    //Constructor which takes an SQLite Database
    public DemoScrapbookDAO(SQLiteDatabase newDataBase) {
        database = newDataBase;
    }

    private Comment queryCommentByID(long id) {
        Cursor c = database.rawQuery("SELECT * FROM Comments WHERE CommentID=?",new String[]{Long.toString(id)});

        if (c.getCount() == 0) {
            return null;
        }

        c.close();


        long timeStamp = c.getLong(4);
        String commentText = c.getString(3);
        User author = null;

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
        }

        //Once Robbie implements the UserDAO, will also add the ability to set the owner of the Scrapbook by
        //querying for its User ID
        User author = null;

        //Get the likes from the result
        long likes = c.getLong(2);

        //Get the title, description and timestamp of the Scrapbook from the result
        String title = c.getString(3);
        String description = c.getString(4);
        long timeStamp = c.getLong(5);

        //Get the location from the latitude and longitude columns of the result
        Location location = new Location(c.getDouble(6),c.getDouble(7));

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
        Set<Scrapbook> result = new HashSet<>();

        Cursor c = database.rawQuery("SELECT ScrapbookID FROM Scrapbooks",new String[]{});
        //Go through every row in the database, converting it into a scrapbook and checking the distance to the
        //starting point - if it's less than or equal to the max, add it to the hashset
        while (c.moveToNext()) {
            Scrapbook nextBook = queryScrapbookByID(c.getLong(0));
            if (nextBook != null && nextBook.getLocation().distanceFrom(start) <= maxDistance) {
                result.add(nextBook);
            }
        }

        //Return the resulting hashset
        return result;
    }

    @Override
    public Set<Scrapbook> queryScrapbooksByUser(User user) {
        Set<Scrapbook> result = new HashSet<>();

        //Get all scrapbook IDs from scrapbooks whose user ID matches with that of the user in the method
        Cursor c = database.rawQuery("SELECT ScrapbookID FROM Scrapbooks WHERE UserID=?",new String[]{Long.toString(user.getID())});
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
        scrapbookValues.put("UserID",scrapbook.getOwner().getID());
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
        entryValues.put("ImageID",entry.getImageResource());
        entryValues.put("Timestamp",entry.getTimeStamp());
        entryValues.put("Caption",entry.getCaption());
        database.insert("Entries",null,entryValues);
    }

    @Override
    public void createComment(Comment comment, long scrapbookID) {
        ContentValues commentValues = new ContentValues();
        commentValues.put("CommentID",comment.getID());
        commentValues.put("AuthorID",comment.getAuthor().getID());
        commentValues.put("ScrapbookID",scrapbookID);
        commentValues.put("CommentText",comment.getContent());
        commentValues.put("Timestamp",comment.getTimeStamp());
        database.insert("Comments",null,commentValues);
    }
}
