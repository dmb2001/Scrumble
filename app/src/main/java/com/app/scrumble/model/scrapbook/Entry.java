package com.app.scrumble.model.scrapbook;

public final class Entry {

    private final long ID;
    private final long timeStamp;
    private String caption;

    public Entry(long ID, long timestamp, String caption){
        this.ID = ID;
        this.timeStamp = timestamp;
        this.caption = caption;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption){
        this.caption = caption;
    }

    public long getID() {
        return ID;
    }

}
