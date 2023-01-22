package com.app.scrumble.model;

public final class Entry {

    private long ID = ++ID_COUNTER;
    private static long ID_COUNTER = 0;

    private long timeStamp;
    private int imageResource;

    private String caption;

    public Entry(long timestamp, int resourceID, String caption){
        this.timeStamp = timestamp;
        this.imageResource = resourceID;
        this.caption = caption;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getCaption() {
        return caption;
    }

    public long getID() {
        return ID;
    }

}
