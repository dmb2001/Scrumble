import java.io.File;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
 
public class upload {
    public static void main(String[] args) {
        String bucketName = "scrumbletest";
        //this folder would be changed to a variable that holds the users id etc for the directory
        String folderName = "";
        //file name will need to be changed to whatever was tapped by the user same with file path 
        String fileName = ".png";
        String filePath = "" + fileName;
        String key = folderName + "/" + fileName;
         
        S3Client client = S3Client.builder().build();
         
        PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl("public-read")
                        .build();
         
        client.putObject(request, RequestBody.fromFile(new File(filePath)));
         
        S3Waiter waiter = client.waiter();
        HeadObjectRequest requestWait = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
         
        WaiterResponse<HeadObjectResponse> waiterResponse = waiter.waitUntilObjectExists(requestWait);
         
        waiterResponse.matched().response().ifPresent(System.out::println);
        //have a query here to upload the filepath and the user id etc to the database
        
        //have this changed to a pop up notification on screen 
        System.out.println("File " + fileName + " was uploaded.");     
    }
}
