/*
This Java program uses the AWS SDK for Java to create an Amazon S3 bucket. It initializes an
S3 client for the US_EAST_1 region using credentials from my AWS profile. Before creating the bucket with the
name "s4059306-mybucket", it checks if a bucket with that name already exists. If the bucket does not exist,
it proceeds to create it and then retrieves and prints the bucket's location. The program includes try-catch
blocks to handle potential exceptions that might occur during interaction with the AWS S3 service or the SDK
client itself.
 */

/*
Code adapted from the Cloud Computing week 3 practicals
 */

package com.amazonaws.samples;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;

import java.io.IOException;

public class CreateBucket2 {

    public static void main(String[] args) throws IOException {
        Regions clientRegion = Regions.US_EAST_1;
        String bucketName = "s4059306-mybucket";

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider())
                    .withRegion(clientRegion)
                    .build();

            if (!s3Client.doesBucketExistV2(bucketName)) {

                s3Client.createBucket(new CreateBucketRequest(bucketName));


                String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
                System.out.println("Bucket location: " + bucketLocation);
            }
        } catch (AmazonServiceException e) {

            e.printStackTrace();
        } catch (SdkClientException e) {

            e.printStackTrace();
        }
    }
}

