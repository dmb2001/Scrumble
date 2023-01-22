package com.app.scrumble.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Scrapbook {

    private static final String TAG_PHOTOGRAPHY = "Photography";
    private static final String TAG_ADVENTURE = "Adventure";
    private static final String TAG_TRAVEL = "Travel";

    public static final long NEVER_UPDATED = -1;

    private final long ID;

    private String title;
    private String description;
    private final User owner;
    private final Location location;
    private final Set<String> tags = new HashSet<>();
    private final Map<Reaction, Integer> reactions = new HashMap<>();
    private final List<Entry> entries = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();

    public Scrapbook(long ID, User owner, Location location){
        this.ID = ID;
        this.owner = owner;
        this.location = location;

        tags.add(TAG_PHOTOGRAPHY);
        tags.add(TAG_ADVENTURE);
        tags.add(TAG_TRAVEL);

        reactions.put(Reaction.HAPPY, 5);
        reactions.put(Reaction.EXCITED, 2);
    }

    public long getID() {
        return ID;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public Entry getEntryByID(long entryID){
        for(Entry entry : entries){
            if (entry.getID() == entryID){
                return entry;
            }
        }
        return null;
    }

    public void addEntry(Entry entry){
        entries.add(entry);
    }

    public Location getLocation() {
        return location;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getCommentCount(){
        return 9;//TODO this is just for prototype purposes
    }

    public int getTagCount(){
        return tags.size();
    }

    public int getReactionCount(){
        return reactions.size();
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return "Lorem ipsum dolor sit amet. Et omnis necessitatibus et deleniti aspernatur qui quia incidunt cum similique recusandae sit dolores omnis. Ut dolorem voluptatem qui deserunt voluptas ut distinctio optio et maxime consequatur." +
                "Ut maiores doloribus qui voluptatem cumque et doloribus soluta in voluptas quas et ducimus quisquam. Ut expedita inventore quo molestiae fugiat non modi repudiandae quo similique deleniti ea porro molestiae." +
                "Eum saepe beatae non beatae delectus sed ipsam reprehenderit. Et commodi nihil ex nihil repellat est tempore sint ut aspernatur sequi ea corrupti voluptatem sed dolorem omnis ut impedit illum.";
    }

    public void setTag(String tag){
        this.tags.add(tag);
    }

    public Set<String> getTags() {
        return tags;
    }

    public User getOwner() {
        return owner;
    }

    public long getLastUpdate(){
        long mostRecentUpdate = NEVER_UPDATED;
        for (Entry entry : entries){
            if(entry.getTimeStamp() > mostRecentUpdate){
                mostRecentUpdate = entry.getTimeStamp();
            }
        }
        return mostRecentUpdate;
    }
}
