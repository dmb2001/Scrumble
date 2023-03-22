package com.app.scrumble.model.group.scrapbook;

import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_CAPTION;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_COMMENT_ID;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_COMMENT_TEXT;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_DESCRIPTION;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_ENTRY_ID;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_LATITUDE;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_LIKES;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_LONGITUDE;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_PARENT_COMMENT_ID;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_SCRAPBOOK_ID;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_TAG_HIDDEN;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_TAG_NAME;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_TIMESTAMP;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_TITLE;
import static com.app.scrumble.model.CustomDatabaseOpenHelper.COLUMN_USER_ID;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.scrumble.model.RemoteDatabaseConnection;
import com.app.scrumble.model.group.Group;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



//TODO for Tikhon: When Robbie finishes implementing UserDAO, update corresponding functions in DemoScrapbookDAO
//Todo-2 for Tikhon: At some point, create validation mechanisms for the updating and removing methods

public class RemoteScrapbookDAO implements ScrapbookDAO{

    private final RemoteDatabaseConnection database;
    private final Scrapbook.ScrapBookBuilder builder = new Scrapbook.ScrapBookBuilder();

    private UserDAO userDAO;

    //Constructor which takes a remote database.
    public RemoteScrapbookDAO(RemoteDatabaseConnection database, UserDAO userDAO) {
        this.database = database;
        this.userDAO = userDAO;
    }

    private Comment constructComment(Map<String,Object> row) {
        Long commentID = null;
        Long timestamp = null;
        String commentText = null;
        User author = null;
        for(Map.Entry<String,Object> entry : commentRow.entrySet()){
            if(entry.getKey().equals("CommentID")) {
                commentID = (long) entry.getValue();
            } else if(entry.getKey().equals("Timestamp")){
                timestamp = (long) entry.getValue();
            } else if (entry.getKey().equals("UserID")){
                long authorID = (long) entry.getValue();
                author = userDAO.queryUserByID(authorID);
            } else if (entry.getKey().equals("CommentText")){
                commentText = (String) entry.getValue();
            }
        }
        return new Comment(commentID,timestamp,commentText,author);
    }

    private Comment queryCommentByID(long commentID) {
        //Get a List of Maps of Strings to objects of comments corresponding to the id.
        List<Map<String,Object>> results = database.executeQuery("Comments", null, "CommentID = ?", new Object[]{commentID});

        //If exactly 1 comment is found, do code related to constructing it
        if (results.size() == 1) {


            //Get the comment from the results, and use a method to construct it into an
            //object
            Map<String,Object> commentRow = results.get(0);
            Comment resultingComment = constructComment(commentRow);

            //Now, query the Comments table for comments whose parentCommentID is the same as
            // the resulting Comment's ID
            List<Map<String,Object>> childCommentResult = database.executeQuery("Comments",null,"ParentCommentID = ?",new Object[]{commentID});
            if (childCommentResult.size() == 0) {
                //If no child comments is found, return the comment with no child comments
                Log.d("DEBUGGING", "Resulting Comment has no children!");
            } else {
                ArrayList<Comment> childComments = new ArrayList<Comment>();

                    for (Map<String,Object> row : childCommentResult) {
                            Comment childComment = constructComment(row);
                            childComments.add(childComment);
                        }

                    resultingComment.addChildren(childComments);
                    Log.d("DEBUGGING", "Added Children to Resulting Comment!");

            }
            return resultingComment;
        }

        //If no comments or more than 1 (error) comments are found, return null
        return null;
    }

    @Override
    public Scrapbook queryScrapbookByID(long scrapbookID) {
        //Get a query based on ID
        List<Map<String,Object>> results = database.executeQuery("Scrapbooks", null, "ScrapbookID = ?", new Object[]{scrapbookID});

        //If the number of scrapbooks found is exactly 1, construct the scrapbooks
        if (results.size() == 1) {
            Map<String,Object> row = results.get(0);

            User author = null;
            Integer likes = null;
            String title = null;
            String description = null;
            Long timestamp = null;
            Double latitude = null;
            Double longitude = null;

            for(Map.Entry<String,Object> entry : row.entrySet()){
                if(entry.getKey().equals("UserID")){
                    long userID = (long) entry.getValue();
                    author = userDAO.queryUserByID(userID);
                } else if (entry.getKey().equals("Likes")){
                    likes = (int) entry.getValue();
                } else if (entry.getKey().equals("Title")){
                    title = (String) entry.getValue();
                } else if (entry.getKey().equals("Description")){
                    description = (String) entry.getValue();
                } else if (entry.getKey().equals("Timestamp")){
                    timestamp = (long) entry.getValue();
                } else if (entry.getKey().equals("Latitude")){
                    latitude = (double) entry.getValue();
                } else if (entry.getKey().equals("Longitude")){
                    longitude = (double) entry.getValue();
                }
            }

            if (latitude == null || longitude == null) {
                Log.d("DEBUGGING:","No latitude or longitude found! Returning null!");
                return null;
            }

            List<Map<String,Object>> commentResults = database.executeQuery("Comments",null,"ScrapbookID = ?",new Object[]{scrapbookID});

            List<Comment> comments = new ArrayList<Comment>();

            for (Map<String,Object> comment : commentResults) {
                comments.add(constructComment(comment));
            }

            Location scrapbookLocation = new Location(latitude,longitude);

            Set<Tag> tags = queryScrapbookTagsByScrapbookID(scrapbookID);

            List<Entry> entries = queryEntriesByScrapBookID(scrapbookID);

            return builder.withID(scrapbookID).withOwner(author).withTitle(title).withDescription(description)
                    .withTimeStamp(timestamp).withLocation(scrapbookLocation).withTags(tags).withEntries(entries).
                    withComments(comments).build();
        }

        return null;
    }

    private Set<Tag> queryScrapbookTagsByScrapbookID(long scrapbookID) {
        List<Map<String,Object>> results = database.executeRawQuery(
                "SELECT Tags.TagName, Tags.Hidden FROM Tags, ScrapbookTags " +
                        "WHERE ScrapbookID = ? AND Tags.TagName = ScrapbookTags.TagName", new Object[]{scrapbookID});

        //If no tags were found, return empty hashset.
        if (results.size() == 0) {
            Log.d("DEBUGGING:","No tags found associated with scrapbook! Returning empty tag set.");
            return new HashSet<Tag>();
        }

        Set<Tag> tags = new HashSet<>();

        //Go through every row in the results, constructing a tag from it and adding it to the hashset
        for(Map<String,Object> row : results) {
            String tagName = null;
            Boolean tagHidden = null;
            for(Map.Entry<String,Object> entry : row.entrySet()){
                if(entry.getKey().equals("TagName")){
                    tagName = (String) entry.getValue();
                } else if (entry.getKey().equals("Hidden")){
                    tagHidden = (boolean) entry.getValue();
                }
            }

            //If the tag name and its hidden-ness were successfully acquired, add a new tag to the set
            if (tagName != null && tagHidden != null) {
                Tag newTag = new Tag(tagName,tagHidden);
                tags.add(newTag);
            }
        }

        return tags;
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
        //TODO Dean said he's 90% finished with his own implementation.
        return null;
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
