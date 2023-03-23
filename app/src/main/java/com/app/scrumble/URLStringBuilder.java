package com.app.scrumble;

public class URLStringBuilder {

    private static final String start = "http://scrumbletest.s3.eu-west-2.amazonaws.com/";

    public static String buildMemoryLocation(long userID, long scrapbookID, long memoryID){
        return start + userID + "/" + scrapbookID + "/" + memoryID;
    }

    public static String buildMemoryKey(long userID, long scrapbookID, long memoryID){
        return userID + "/" + scrapbookID + "/" + memoryID;
    }

    public static String buildProfilePictureLocation(long userID){
        return start + userID + "/profile_picture";
    }

    public static String buildProfilePictureKey(long userID){
        return userID + "/profile_picture";
    }

    public static String buildGroupCoverArtLocation(long groupID){
        return start + groupID + "/cover";
    }

    public static String buildGroupCoverArtKey(long groupID){
        return groupID + "/cover";
    }

}
