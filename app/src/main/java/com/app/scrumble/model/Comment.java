package com.app.scrumble.model;

public class Comment {

    private final long timeStamp;
    private final User author;

    private final int resourceID;

    public Comment(long timeStamp, User author, int resourceID){
        this.timeStamp = timeStamp;
        this.author = author;
        this.resourceID = resourceID;
    }

//    public String getBody() {
//        return body;
//    }


    public int getResourceID() {
        return resourceID;
    }

//    public void setBody(String body) {
//        this.body = body;
//    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public User getAuthor() {
        return author;
    }

}
