package com.app.scrumble.model.scrapbook;

import com.app.scrumble.model.user.User;

import java.util.List;
import java.util.Set;

public class Comment {

    private final long id;
    private final long timeStamp;
    private final String content;
    private final User author;

    private final List<Comment> children;

    public Comment(long id, long timeStamp, String content, User author){
        this(id, timeStamp, content, author, null);
    }

    public Comment(long id, long timeStamp, String content, User author, List<Comment> children){
        this.id = id;
        this.timeStamp = timeStamp;
        this.content = content;
        this.author = author;
        this.children = children;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getContent(){
        return this.content;
    }

    public User getAuthor() {
        return author;
    }

    public boolean hasChildren(){
        return children != null && children.size() > 0;
    }

    public int getChildCount(){
        return children == null ? 0 : children.size();
    }

    public long getID() {
        return id;
    }
}
