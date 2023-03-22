package com.app.scrumble.model.group.scrapbook;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.app.scrumble.model.CustomDatabaseOpenHelper.*;



//TODO for Tikhon: When Robbie finishes implementing UserDAO, update corresponding functions in DemoScrapbookDAO
//Todo-2 for Tikhon: At some point, create validation mechanisms for the updating and removing methods

public class LocalScrapbookDAO implements ScrapbookDAO{

    private final SQLiteDatabase database;
    private final Scrapbook.ScrapBookBuilder builder = new Scrapbook.ScrapBookBuilder();

    private UserDAO userDAO;

    //Constructor which takes an SQLite Database
    public LocalScrapbookDAO(SQLiteDatabase newDataBase, UserDAO userDAO) {
        database = newDataBase;
        this.userDAO = userDAO;
    }

    private Comment queryCommentByID(long id) {
        Cursor c = database.rawQuery("SELECT * FROM Comments WHERE " + COLUMN_COMMENT_ID + " =?",new String[]{Long.toString(id)});

        if (c.getCount() == 0) {
            return null;
        }

        c.close();

        long timeStamp = c.getLong(4);
        String commentText = c.getString(3);

        //Get the author from the UserDAO
        User author = userDAO.queryUserByID(c.getLong(1));

        //Now query the ParentChildComments Table for any children of this comment (recursively)
        Cursor c2 = database.rawQuery("SELECT * FROM ParentChildComments WHERE " + COLUMN_PARENT_COMMENT_ID + " =?",new String[]{Long.toString(id)});
        if (c2.getCount() == 0) {
            //If there are no child comments, return the comment with the constructor without children
             return new Comment(id,timeStamp,commentText,author);
        } else {
            c2.moveToFirst();
            //Otherwise, create a list, and recursively call this function on all child comments
            List<Comment> childrenComments = new ArrayList<>();
            while (true) {
                childrenComments.add(queryCommentByID(c2.getLong(1)));
                c2.moveToNext();
                if (c2.isAfterLast()) {
                    break;
                }
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

        c.moveToFirst();

        //Get the author by using the UserDAO implementation to query a User by ID
        User author = userDAO.queryUserByID(c.getLong(c.getColumnIndex(COLUMN_USER_ID)));

        //Get the likes from the result
        long likes = c.getLong(c.getColumnIndex(COLUMN_LIKES));

        //Get the title, description and timestamp of the Scrapbook from the result
        String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
        long timeStamp = c.getLong(c.getColumnIndex(COLUMN_TIMESTAMP));

        //Get the location from the latitude and longitude columns of the result
        Location location = new Location(c.getDouble(c.getColumnIndex(COLUMN_LATITUDE)),c.getDouble(c.getColumnIndex(COLUMN_LONGITUDE)));

        //Get the tags by querying ScrapbookTags and Tags
        Set<Tag> tags = queryScrapbookTagsByScrapbookID(id);

        //Close the Cursor
        c.close();

        //Get the Comments
        List<Comment> comments = queryScrapbookComments(id, null);

        //Return the built scrapbook
        return builder.withID(id).withOwner(author).withTitle(title).withDescription(description)
                .withTimeStamp(timeStamp).withLocation(location).withTags(tags).withEntries(queryEntriesByScrapBookID(id)).withComments(comments).build();
    }

    private Set<Tag> queryScrapbookTagsByScrapbookID(long scrapbookID) {
        Cursor c = database.rawQuery(
                "SELECT Tags." + COLUMN_TAG_NAME + ", Tags." + COLUMN_TAG_HIDDEN + " FROM Tags, ScrapbookTags " +
                        "WHERE " + COLUMN_SCRAPBOOK_ID + " = ? " +
                        "AND Tags." + COLUMN_TAG_NAME + " = ScrapbookTags." + COLUMN_TAG_NAME
        , new String[]{Long.toString(scrapbookID)});

        if(c.getCount() > 0) {
            Set<Tag> tags = new HashSet<>();
            while (c.moveToNext()) {
                String tname = c.getString(c.getColumnIndex(COLUMN_TAG_NAME));
                boolean thidden = c.getInt(c.getColumnIndex(COLUMN_TAG_HIDDEN)) == 1;
                Tag tag = new Tag(tname, thidden);
                tags.add(tag);
            }

            return tags;
        } else {
            Log.d("DEBUGGING", "no tag match found! returning empty set");
            return new HashSet<Tag>();
        }
    }

    private List<Comment> queryScrapbookComments(long scrapbookID, Comment parentComment){
        Cursor c;
        if(parentComment == null){
            c = database.rawQuery("SELECT * FROM Comments WHERE " + COLUMN_SCRAPBOOK_ID + "=? AND " + COLUMN_PARENT_COMMENT_ID + " IS NULL ORDER BY " + COLUMN_TIMESTAMP + " DESC",new String[]{Long.toString(scrapbookID)});
        }else{
            c = database.rawQuery("SELECT * FROM Comments WHERE " + COLUMN_SCRAPBOOK_ID + "=? AND " + COLUMN_PARENT_COMMENT_ID + "=? ORDER BY " + COLUMN_TIMESTAMP + " DESC",new String[]{Long.toString(scrapbookID), Long.toString(parentComment.getId())});
        }

        c.moveToFirst();

        if(c.getCount() > 0){
            Log.d("DEBUGGING", "there are: " + c.getCount() + " records");
            List<Comment> comments = new ArrayList<>();
            while(true){
                Comment comment = new Comment(c.getLong(c.getColumnIndex(COLUMN_COMMENT_ID)), c.getLong(c.getColumnIndex(COLUMN_TIMESTAMP)), c.getString(c.getColumnIndex(COLUMN_COMMENT_TEXT)), userDAO.queryUserByID(c.getLong(c.getColumnIndex(COLUMN_USER_ID))));
                List<Comment> childComments = queryScrapbookComments(scrapbookID, comment);
                if(childComments != null){
                    comment.addChildren(childComments);
                }
                comments.add(comment);
                c.moveToNext();
                if (c.isAfterLast()) {
                    break;
                }
            }
            return comments;
        }else{
            Log.d("DEBUGGING", "there are comment no records matching this query!");
            return null;
        }

    }

    private List<Entry> queryEntriesByScrapBookID(long scrapbookID){
        Cursor c = database.rawQuery("SELECT * FROM Entries WHERE " + COLUMN_SCRAPBOOK_ID + "=? ORDER BY " + COLUMN_TIMESTAMP + " DESC", new String[]{Long.toString(scrapbookID)});
        if(c.getCount() == 0){
            return new ArrayList<Entry>(); //If there are no entries, return an empty ArrayList of Entries
        }else{
            Log.d("DEBUGGING", "cursor has " + c.getCount() + " entries");
            List<Entry> memories = new ArrayList<>();

            c.moveToFirst();
            while (true){
                Entry memory = new Entry(c.getLong(c.getColumnIndex(COLUMN_ENTRY_ID)), c.getLong(c.getColumnIndex(COLUMN_TIMESTAMP)), c.getString(c.getColumnIndex(COLUMN_CAPTION)));
                memories.add(memory);
                c.moveToNext();
                if (c.isAfterLast()) {
                    break;
                }
            }
            return memories;
        }
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
                Location location = new Location(c.getDouble(c.getColumnIndex(COLUMN_LATITUDE)),c.getDouble(c.getColumnIndex(COLUMN_LONGITUDE)));
                Log.d("DEBUGGING", "distance from stored Scrapbook: " + Location.distanceBetween(start, location));
                if(Location.distanceBetween(start, location) <= maxDistance){
                    result.add(
                            new Scrapbook.ScrapBookBuilder()
                                    .withID(c.getLong(c.getColumnIndex(COLUMN_SCRAPBOOK_ID)))
                                    .withOwner(userDAO.queryUserByID(c.getLong(c.getColumnIndex(COLUMN_USER_ID))))
                                    .withLocation(location)
                                    .withTitle(c.getString(c.getColumnIndex(COLUMN_TITLE)))
                                    .withDescription(c.getString(c.getColumnIndex(COLUMN_DESCRIPTION)))
                                    .withEntries(queryEntriesByScrapBookID(c.getLong(c.getColumnIndex(COLUMN_SCRAPBOOK_ID))))
                                    .withComments(queryScrapbookComments(c.getColumnIndex(COLUMN_SCRAPBOOK_ID), null))
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
        long insertedAt = database.insert("Scrapbooks",null,scrapbookValues);
        if(insertedAt != -1 && scrapbook.getEntries() != null){
            for (Entry entry : scrapbook.getEntries()){
                createEntry(entry, scrapbook.getID());
            }
        }
        if(insertedAt != -1 && scrapbook.getTags() != null) {
            for (Tag tag : scrapbook.getTags()) {
                createTag(tag, scrapbook.getID());
            }
        }
    }

    @Override
    public void deleteScrapbook(long id) {
        database.delete("Scrapbooks","ScrapbookID=?",new String[]{Long.toString(id)});
    }

    @Override
    public void createEntry(Entry entry, long scrapbookID) {
        ContentValues entryValues = new ContentValues();
        entryValues.put(COLUMN_SCRAPBOOK_ID,scrapbookID);
        entryValues.put(COLUMN_ENTRY_ID,entry.getID());
        entryValues.put(COLUMN_TIMESTAMP,entry.getTimeStamp());
        entryValues.put(COLUMN_CAPTION,entry.getCaption());
        long result = database.insert("Entries",null,entryValues);
        if(result < 0){
            Log.d("DEBUGGING", "Error inserting comment!");
        }else{
            Log.d("DEBUGGING", "comment inserted at: " + result);
        }
    }

    private void createTag(Tag tag, long scrapbookID) {
        // insert tag into Tags table
        ContentValues tagValues = new ContentValues();
        tagValues.put(COLUMN_TAG_NAME,tag.getName());
        tagValues.put(COLUMN_TAG_HIDDEN, (tag.isHidden() ? 1 : 0)); // true = 1, false = 0
        long result = database.insert("Tags",null,tagValues);

        // insert tag and scrapbook into ScrapbookTags
        ContentValues scTagValues = new ContentValues();
        scTagValues.put(COLUMN_SCRAPBOOK_ID, scrapbookID);
        scTagValues.put(COLUMN_TAG_NAME,tag.getName());
        long result2 = database.insert("ScrapbookTags",null,scTagValues);
    }

    @Override
    public void createComment(Comment comment, long scrapbookID, Long parentCommentID) {
        ContentValues commentValues = new ContentValues();
        commentValues.put(COLUMN_COMMENT_ID,comment.getID());
        commentValues.put(COLUMN_USER_ID,comment.getAuthor().getId());
        commentValues.put(COLUMN_SCRAPBOOK_ID,scrapbookID);
        commentValues.put(COLUMN_COMMENT_TEXT,comment.getContent());
        commentValues.put(COLUMN_TIMESTAMP,comment.getTimeStamp());
        commentValues.put(COLUMN_PARENT_COMMENT_ID, parentCommentID);
        long result = database.insert("Comments",null,commentValues);
        if(result < 0){
            Log.d("DEBUGGING", "Error inserting comment!");
        }else{
            Log.d("DEBUGGING", "comment inserted at: " + result);
        }
    }

    @Override
    public List<Scrapbook> getRecentScrapbooksFor(List<User> users, int limit) {
        return new ArrayList<>();
    }

    @Override
    public List<Scrapbook> getScrapbooksForGroup(String tag, Location origin, long maxDistance) {
        return new ArrayList<>();
    }
}
