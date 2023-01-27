package com.app.scrumble.model.scrapbook;

import com.app.scrumble.model.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Use a {@link ScrapBookBuilder} to build instances of this class
 */
public class Scrapbook {

    public static final long NEVER_UPDATED = -1;
    public static final String UNTITLED = "Untitled";
    public static final String NO_DESCRIPTION = "No description";

    private Long ID;

    private String title = UNTITLED;
    private String description = NO_DESCRIPTION;
    private User owner;
    private Location location;
    private Set<String> tags = new HashSet<>();
    private Map<Reaction, Integer> reactions = new HashMap<>();
    private List<Entry> entries = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    private Scrapbook(){}

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

    public Location getLocation() {
        return location;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getCommentCount(){
        return comments.size();
    }

    public int getTagCount(){
        return tags.size();
    }

    public int getReactionCount(){
        return reactions.size();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

    /**
     * use this class to build scrapbooks. You must provide an ID, owner and location for the Scrapbook at the least. Provides a fluent interface so you can chain method calls eg:
     *
     * Scrapbook scrapbook = new ScrapBookBuilder().withID(123).withTitle("example_title").build();
     */
    public static final class ScrapBookBuilder{

        private Scrapbook scrapbook;

        public ScrapBookBuilder withID(long id){
            this.scrapbook.ID = id;
            return this;
        }

        public ScrapBookBuilder withTitle(String title){
            Objects.requireNonNull(title);
            this.scrapbook.title = title;
            return this;
        }

        public ScrapBookBuilder withDescription(String description){
            Objects.requireNonNull(description);
            this.scrapbook.description = description;
            return this;
        }

        public ScrapBookBuilder withOwner(User owner){
            Objects.requireNonNull(owner);
            this.scrapbook.owner = owner;
            return this;
        }

        public ScrapBookBuilder withLocation(Location location){
            Objects.requireNonNull(location);
            this.scrapbook.location = location;
            return this;
        }

        public ScrapBookBuilder withTags(Set<String> tags){
            Objects.requireNonNull(tags);
            this.scrapbook.tags = tags;
            return this;
        }

        public ScrapBookBuilder withReactions(Map<Reaction, Integer> reactions){
            Objects.requireNonNull(reactions);
            this.scrapbook.reactions = reactions;
            return this;
        }

        public ScrapBookBuilder withEntries(List<Entry> entries){
            Objects.requireNonNull(entries);
            this.scrapbook.entries = entries;
            return this;
        }

        public ScrapBookBuilder withComments(List<Comment> comments){
            Objects.requireNonNull(comments);
            this.scrapbook.comments = comments;
            return this;
        }

        public Scrapbook build(){
            Objects.requireNonNull(scrapbook.ID);
            Objects.requireNonNull(scrapbook.owner);
            Objects.requireNonNull(scrapbook.location);

            Scrapbook configuredScrapbook = this.scrapbook;
            this.scrapbook = null;

            return configuredScrapbook;
        }

    }
}
