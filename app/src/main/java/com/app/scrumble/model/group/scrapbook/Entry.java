package com.app.scrumble.model.group.scrapbook;

public final class Entry {

    private Long ID;
    private final long timeStamp;
    private String caption;

    public Entry(Long ID, long timestamp, String caption){
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

    public void setID(long ID){
        this.ID = ID;
    }

}
