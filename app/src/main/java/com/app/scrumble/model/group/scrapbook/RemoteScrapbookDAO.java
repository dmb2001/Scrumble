package com.app.scrumble.model.group.scrapbook;

import android.util.Log;

import com.app.scrumble.model.RemoteDatabaseConnection;
import com.app.scrumble.model.group.scrapbook.Scrapbook.ScrapBookBuilder;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.User.UserBuilder;
import com.app.scrumble.model.user.UserDAO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
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
        for(Map.Entry<String,Object> entry : row.entrySet()){
            if(entry.getKey().equals("CommentID")) {
                commentID = new Long((int)entry.getValue());
            } else if(entry.getKey().equals("Timestamp")){
                Timestamp sqlTimestamp = (Timestamp)entry.getValue();
                timestamp = sqlTimestamp.getTime();
            } else if (entry.getKey().equals("UserID")){
                long authorID = new Long((int)entry.getValue());
                author = userDAO.queryUserByID(authorID);
            } else if (entry.getKey().equals("CommentText")){
                commentText = (String) entry.getValue();
            }
        }
        Comment comment = new Comment(commentID,timestamp,commentText,author);

        List<Map<String,Object>> childCommentResult = database.executeQuery("Comments",null,"ParentCommentID = ?",new Object[]{commentID});
        if(!childCommentResult.isEmpty()){
            List<Comment> children = new ArrayList<>();
            for (Map<String,Object> cc : childCommentResult){
                children.add(constructComment(cc));
            }
            comment.addChildren(children);
        }
        return comment;
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
                    long userID = (long) (int)entry.getValue();
                    author = userDAO.queryUserByID(userID);
                } else if (entry.getKey().equals("Likes")){
                    likes = (int) entry.getValue();
                } else if (entry.getKey().equals("Title")){
                    title = (String) entry.getValue();
                } else if (entry.getKey().equals("Description")){
                    description = (String) entry.getValue();
                } else if (entry.getKey().equals("Timestamp")){
                    Timestamp sqlTimestamp = (Timestamp)entry.getValue();
                    timestamp = sqlTimestamp.getTime();
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

            List<Map<String,Object>> topLevelCommentsRows = database.executeQuery("Comments",null,"ScrapbookID = ? AND ParentCommentID IS NULL",new Object[]{scrapbookID});

            Log.d("DEBUGGING", "There were " + topLevelCommentsRows.size() + " top level comments");

            List<Comment> topLevelComments = new ArrayList<Comment>();

            for (Map<String,Object> comment : topLevelCommentsRows) {
                topLevelComments.add(constructComment(comment));
            }

            Location scrapbookLocation = new Location(latitude,longitude);

            Set<Tag> tags = queryScrapbookTagsByScrapbookID(scrapbookID);

            List<Entry> entries = queryEntriesByScrapBookID(scrapbookID);

            return builder.withID(scrapbookID).withOwner(author).withTitle(title).withDescription(description)
                    .withTimeStamp(timestamp).withLocation(scrapbookLocation).withTags(tags).withEntries(entries).
                    withComments(topLevelComments).build();
        }

        return null;
    }

    private Set<Tag> queryScrapbookTagsByScrapbookID(long scrapbookID) {
        List<Map<String,Object>> results = database.executeRawQuery(
                "SELECT Tags.TagName, Tags.TagHidden FROM Tags, ScrapbookTags " +
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

    private List<Entry> queryEntriesByScrapBookID(long scrapbookID) {
        List<Map<String, Object>> q = database.executeQuery("Entries", null, "ScrapbookID=?", new Object[]{scrapbookID});

        if (q.size() == 0) {
            return new ArrayList<Entry>(); //If there are no entries, return an empty ArrayList of Entries
        } else {
            Log.d("DEBUGGING", "query returned " + q.size() + " entries");
            List<Entry> memories = new ArrayList<>();

            for (Map<String,Object> row : q) {
                Entry memory = new Entry(new Long((int)row.get("EntryID")) , ((Timestamp) row.get("Timestamp")).getTime(), (String) row.get("Caption"));
                memories.add(memory);
            }
            return memories;
        }
    }

    @Override
    public Set<Scrapbook> queryScrapbooksByLocation(Location start, long maxDistance) {
        //param order: latitude, latitude, longitude, radius
        double radiusKm = maxDistance / 1000.0;
        String sql = "SELECT Scrapbooks.*, Users.* " +
                "FROM Scrapbooks " +
                "JOIN Users ON Scrapbooks.UserID = Users.UserID " +
                "WHERE 6371 * 2 * ASIN(SQRT(" +
                "    POWER(SIN((Scrapbooks.Latitude - ?) * PI() / 180 / 2), 2) + " +
                "    COS(Scrapbooks.Latitude * PI() / 180) * COS(? * PI() / 180) * " +
                "    POWER(SIN((Scrapbooks.Longitude - ?) * PI() / 180 / 2), 2) " +
                ")) <= ?";
        List<Map<String, Object>> rows = database.executeRawQuery(sql, new Object[]{start.getLatitude(), start.getLatitude(), start.getLongitude(), radiusKm});
        Set<Scrapbook> scrapbooks = new HashSet<>();
        Double latitude = null;
        Double longitude = null;
        for(Map<String,Object> row : rows){
            Scrapbook.ScrapBookBuilder scrapBookBuilder = new ScrapBookBuilder();
            User.UserBuilder userBuilder = new UserBuilder();
            for(Map.Entry<String, Object> entry : row.entrySet()){
                if(entry.getKey().equals("ScrapbookID")){
                    scrapBookBuilder.withID(((Integer) entry.getValue()).longValue());
                }else if(entry.getKey().equals("Title")){
                    scrapBookBuilder.withTitle((String) entry.getValue());
                }else if(entry.getKey().equals("Timestamp")){
                    scrapBookBuilder.withTimeStamp(((Timestamp) row.get("Timestamp")).getTime());
                }else if(entry.getKey().equals("Latitude")){
                    latitude = (Double) entry.getValue();
                }else if(entry.getKey().equals("Longitude")){
                    longitude = (Double) entry.getValue();
                }else if(entry.getKey().equals("UserID")){
                    userBuilder.withID( ((Integer) entry.getValue()).longValue());
                }else if(entry.getKey().equals("Name")){
                    userBuilder.withName((String) entry.getValue());
                }else if(entry.getKey().equals("EmailAddress")){
                    userBuilder.withEmail((String) entry.getValue());
                }else if(entry.getKey().equals("Password")){
                    userBuilder.withPassword((String) entry.getValue());
                }else if(entry.getKey().equals("TypeUser")){
                    String storedType = (String) entry.getValue();
                    if(storedType.equals("User")){
                        userBuilder.withUserType(User.TYPE_USER);
                    }else {
                        userBuilder.withUserType(User.TYPE_ADMIN);
                    }
                }
            }
            scrapBookBuilder.withLocation(new Location(latitude, longitude)).withOwner(userBuilder.build());
            scrapbooks.add(scrapBookBuilder.build());
        }
        return scrapbooks;
    }

    @Override
    public Set<Scrapbook> queryScrapbooksByUser(User user) {
        Set<Scrapbook> scrapbooks = new HashSet<>();

        //Get a query based on User's ID
        List<Map<String,Object>> results = database.executeRawQuery(
                "SELECT ScrapbookID FROM Scrapbooks WHERE UserID = ?", new Object[]{user.getId()});

        for (Map<String,Object> row : results) {
            for (Map.Entry<String,Object> entry : row.entrySet()) {
                if (entry.getKey().equals("ScrapbookID")) {
                    long scrapbookID = (long) entry.getValue();
                    Scrapbook newScrapbook = queryScrapbookByID(scrapbookID);
                    scrapbooks.add(newScrapbook);
                }
            }
        }

        return scrapbooks;
    }

    @Override
    public void createScrapbook(Scrapbook scrapbook) {
        RemoteDatabaseConnection.InsertResult result =
                database.executeInsert("Scrapbooks", new String[]{"UserID","Likes","Title","Description","Timestamp","Latitude","Longitude"},
                        new Object[]{scrapbook.getOwner().getId(), scrapbook.getLikes(), scrapbook.getTitle(),
                                scrapbook.getDescription(),new Timestamp(scrapbook.getTimestamp()),scrapbook.getLocation().getLatitude(),
                                scrapbook.getLocation().getLongitude()});
        scrapbook.setID(result.getGeneratedID());


        scrapbook.setID(result.getGeneratedID());

        if(scrapbook.getEntries() != null){
            for (Entry entry : scrapbook.getEntries()){
                createEntry(entry, scrapbook.getID());
            }
        }

        if(scrapbook.getTagCount() > 0) {
            for (Tag tag : scrapbook.getTags()) {
                createTag(tag, scrapbook.getID());
            }
        }

        Log.d("DEBUGGING", "Scrapbook Insert Result: " + result.isSuccessful() + " Generated Key: " + result.getGeneratedID());
    }

    @Override
    public void deleteScrapbook(long scrapbookID) {
        database.executeDelete("Scrapbooks", "ScrapbookID = ?", new Object[]{scrapbookID});
    }

    @Override
    public void createEntry(Entry entry, long scrapbookID) {
        RemoteDatabaseConnection.InsertResult result =
                database.executeInsert("Entries", new String[]{"ScrapbookID","Timestamp","Caption"},
                        new Object[]{scrapbookID, new Timestamp(entry.getTimeStamp()), entry.getCaption()});
        entry.setID(result.getGeneratedID());
        Log.d("DEBUGGING:", "Entry Insert Result: " + result.isSuccessful() + " Generated Key: " + result.getGeneratedID());
    }

    private void createTag(Tag tag, long scrapbookID) {
        //Create a new Tag in the Tags table, in case it doesn't exist
        try {
            RemoteDatabaseConnection.InsertResult tagCreateResult =
                    database.executeInsert("Tags", new String[]{"TagName", "TagHidden"},
                            new Object[]{tag.getName(), tag.isHidden()});
            Log.d("DEBUGGING:", "Tag Insert Result: " + tagCreateResult.isSuccessful());
        } catch (RemoteDatabaseConnection.DatabaseException e) {
            Log.d("DEBUGGING",e.getMessage());
        }

        //Create a new association between this Tag and the Scrapbook in the ScrapbookTags table
        RemoteDatabaseConnection.InsertResult tagAssociateResult =
                database.executeInsert("ScrapbookTags", new String[]{"ScrapbookID","TagName"},
                        new Object[]{scrapbookID, tag.getName()});
        Log.d("DEBUGGING:", "ScrapbookTag Insert Result: " + tagAssociateResult.isSuccessful());
    }

    @Override
    public void createComment(Comment comment, long scrapbookID, Long parentCommentID) {
        RemoteDatabaseConnection.InsertResult result =
                database.executeInsert("Comments",
                        new String[]{"UserID","ScrapbookID","Comment","Timestamp","ParentCommentID"},
                        new Object[]{comment.getAuthor().getId(),scrapbookID,comment.getContent(),new Timestamp(comment.getTimeStamp()),
                            parentCommentID});
        Log.d("DEBUGGING:", "Tag Insert Result: " + result.isSuccessful() + " Generated Key: " + result.getGeneratedID());
    }

    @Override
    public List<Scrapbook> getRecentScrapbooksFor(List<User> users, int limit) {
        String whereClause = "";
        LinkedList<Object> uids = new LinkedList<>(); // params for query

        if (!users.isEmpty()) {
            for (User u : users) {
                if (whereClause.isEmpty()) {
                    whereClause = "UserID = ?";
                } else {
                    whereClause += " OR UserID = ?";
                }

                uids.add(u.getId()); // add userids to params
            }

            // add limit to end of params
            uids.add(limit);

            // execute query
            List<Map<String, Object>> q = database.executeRawQuery(
                    "SELECT ScrapbookID FROM Scrapbooks" +
                            " WHERE " + whereClause +
                            " ORDER BY Timestamp DESC" +
                            " LIMIT ?;"
                    , uids.toArray());

            if (q.size() > 0) {
                List<Scrapbook> scrapbooks = new ArrayList<>();

                for (Map<String, Object> row : q) {
                    long id = (long) row.get("ScrapbookID");
                    Scrapbook scrapbook = queryScrapbookByID(id);
                    scrapbooks.add(scrapbook);
                }

                return scrapbooks;

            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Scrapbook> getScrapbooksForGroup(String tag, Location origin, long maxDistance) {
        List<Map<String, Object>> q = database.executeRawQuery(
                "SELECT Latitude, Longitude, ScrapbookID FROM Scrapbooks" +
                        " LEFT JOIN ScrapbookGroups" +
                        " ON Scrapbooks.ScrapbookID = ScrapbookGroups.ScrapbookID" +
                        " WHERE ScrapbookGroups.GroupID = ?"
                , new Object[]{tag});

        List<Scrapbook> scrapbooks = new ArrayList<>();
        Log.d("DEBUGGING:", "Local Scrapbook Size: " + q.size());

        for (Map<String,Object> row : q) {
            double lat = (double) row.get("Latitude");
            double longi = (double) row.get("Longitude");
            Location bookLocation = new Location(lat, longi);

            long dist = Location.distanceBetween(origin, bookLocation);
            if (dist <= maxDistance) {
                long id = (long) row.get("ScrapbookID");
                Scrapbook scrapbook = queryScrapbookByID(id);
                scrapbooks.add(scrapbook);
                Log.d("DEBUGGING:", "Scrapbook ID: " + id + ", Distance: " + dist);
            }
        }

        return scrapbooks;
    }
}
