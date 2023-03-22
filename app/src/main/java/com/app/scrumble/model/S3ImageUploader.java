package com.app.scrumble.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.app.scrumble.Scrumble;

import java.io.File;

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

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = new AmazonS3Client(credentials, Region.getRegion(region));

        TransferUtility transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(context)
                .build();

        TransferObserver uploadObserver = transferUtility.upload(bucketName, name, getFileFromUri(uri));

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
                // Handle errors
            }
        });
    }

    @Override
    public void cancel() {

    }

    private File getFileFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String realPath = cursor.getString(columnIndex);
        cursor.close();
        return new File(realPath);
    }

}
