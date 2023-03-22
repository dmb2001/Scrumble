package com.app.scrumble.model;

import android.net.Uri;

public interface ImageUploader {

    void upload(Uri location, String name) throws FileUploadException;

    void cancel();

    public static final class FileUploadException extends RuntimeException{

    }

}
