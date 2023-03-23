package com.app.scrumble.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.app.scrumble.Scrumble;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link ImageUploader} used to upload images to an Amazon S3 bucket. These are single use! Request a new uploader using {@link Scrumble#getImageUploader()} for each image you want to upload.
 */
public class S3ImageUploader implements ImageUploader{

    private final Context context;

    private static final String accessKey = "AKIATFVYBAKIPWEHUKOR";
    private static final String secretKey = "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";
    private static final String region = "eu-west-2";
    private static final String bucketName = "scrumbletest";

    private AmazonS3Client s3Client;

    public S3ImageUploader(Context context){
        this.context = context;
    }

    @Override
    public void upload(Uri uri, String name) throws FileUploadException{
        if(s3Client != null){
            throw new IllegalStateException("Only use instances of S3ImageUploader once! To upload another image, create a new uploader");
        }
        Log.d("DEBUGGING", "uploading using object key: ." + name + ".");

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = new AmazonS3Client(credentials, Region.getRegion(region));

        TransferUtility transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(context)
                .build();

        try{
            TransferObserver uploadObserver = transferUtility.upload(bucketName, name, getFileFromUri(uri), CannedAccessControlList.PublicRead);

            uploadObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        Log.d("DEBUGGING", "File transfer complete!");
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    // Update progress bar
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.d("DEBUGGING", "Error transferring image!");
                    // Handle errors
                }
            });
        }catch (Exception e){
            throw new FileUploadException();
        }
    }

    @Override
    public void cancel() {

    }

//    private File getFileFromUri(Uri uri) {
//        String[] projection = { MediaStore.Images.Media.DATA };
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (cursor == null) {
//            return null;
//        }
//        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        String realPath = cursor.getString(columnIndex);
//        cursor.close();
//        return new File(realPath);
//    }

    private File getFileFromUri(Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        String displayName = getDisplayNameFromUri(uri);
        String extension = getFileExtensionFromUri(uri);
        String mimeType = contentResolver.getType(uri);
        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream == null) {
            return null;
        }
        File tempFile = File.createTempFile(displayName, "." + extension, context.getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private String getDisplayNameFromUri(Uri uri) {
        String displayName = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        if (displayName == null) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    private String getFileExtensionFromUri(Uri uri) {
        String extension = null;
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType != null && mimeType.lastIndexOf('/') != -1) {
            extension = mimeType.substring(mimeType.lastIndexOf('/') + 1);
        }
        if (extension == null) {
            extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        }
        if (extension == null) {
            extension = "";
        }
        return extension;
    }

}
