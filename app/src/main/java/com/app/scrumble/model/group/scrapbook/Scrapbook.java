package com.app.scrumble.model.group.scrapbook;

import com.app.scrumble.model.user.User;

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

    private long likes;

    private String title = UNTITLED;
    private String description = NO_DESCRIPTION;
    private User owner;
    private Location location;
    private Set<Tag> tags;
    private Map<Reaction, Integer> reactions;
    private List<Entry> entries;
    private List<Comment> comments;

    private long timeStamp;

    private Scrapbook(){}

    public long getID() {
        return ID;
    }

    public void setID(long ID){
        this.ID = ID;
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

    public long getLikes() {
        return likes;
    }

    public long getTimestamp() {
        return timeStamp;
    }

    public Location getLocation() {
        return location;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getCommentCount(){
        return comments == null ? 0 : comments.size();
    }

    public int getTagCount(){
        return tags == null ? 0 : tags.size();
    }

    public int getEntryCount() {
        return entries == null ? 0 : entries.size();
    }

    public int getReactionCount(){
        return reactions == null ? 0 : reactions.size();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public User getOwner() {
        return owner;
    }

    public long getLastUpdate(){
        long mostRecentUpdate = NEVER_UPDATED;
        if(entries != null){
            for (Entry entry : entries){
                if(entry.getTimeStamp() > mostRecentUpdate){
                    mostRecentUpdate = entry.getTimeStamp();
                }
            }
        }
        return mostRecentUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scrapbook scrapbook = (Scrapbook) o;
        return likes == scrapbook.likes && timeStamp == scrapbook.timeStamp && ID.equals(scrapbook.ID) && Objects.equals(title, scrapbook.title) && Objects.equals(description, scrapbook.description) && owner.equals(scrapbook.owner) && location.equals(scrapbook.location) && Objects.equals(tags, scrapbook.tags) && Objects.equals(reactions, scrapbook.reactions) && Objects.equals(entries, scrapbook.entries) && Objects.equals(comments, scrapbook.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, likes, title, description, owner, location, tags, reactions, entries, comments, timeStamp);
    }

    /**
     * use this class to build scrapbooks. You must provide an ID, owner and location for the Scrapbook at the least. Provides a fluent interface so you can chain method calls eg:
     *
     * Scrapbook scrapbook = new ScrapBookBuilder().withID(123).withTitle("example_title").build();
     */
    public static final class ScrapBookBuilder{

        private Scrapbook scrapbook = new Scrapbook();

        public ScrapBookBuilder withID(long id){
            this.scrapbook.ID = id;
            return this;
        }

        public ScrapBookBuilder withTitle(String title){
            Objects.requireNonNull(title);
            this.scrapbook.title = title;
            return this;
        }

        public ScrapBookBuilder withLikes(int likes){
            Objects.requireNonNull(likes);
            this.scrapbook.likes = likes;
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

        public ScrapBookBuilder withTags(Set<Tag> tags){
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
            this.scrapbook.entries = entries;
            return this;
        }

        public ScrapBookBuilder withComments(List<Comment> comments){
            this.scrapbook.comments = comments;
            return this;
        }

        public ScrapBookBuilder withTimeStamp(long timeStampey){
            Objects.requireNonNull(timeStampey);
            this.scrapbook.timeStamp = timeStampey;
            return this;
        }

        public Scrapbook build(){
            Objects.requireNonNull(scrapbook.owner);
            Objects.requireNonNull(scrapbook.location);

            Scrapbook configuredScrapbook = this.scrapbook;
            this.scrapbook = new Scrapbook();

            return configuredScrapbook;
        }

    }
}
