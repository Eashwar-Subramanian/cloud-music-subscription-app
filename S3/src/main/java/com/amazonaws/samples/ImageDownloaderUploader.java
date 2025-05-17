/*
This Java program, reads a JSON file to extract image URLs associated with artists. For each
image URL, it downloads the image and then uploads it to a specified Amazon S3 bucket ("s4059306-mybucket").
The images are named based on the artist's name (with spaces removed and converted to lowercase, and a ".jpg"
extension). The program uses the AWS SDK for Java to interact with S3 and the org.json library to parse the JSON
data. It includes helper methods for downloading the image from a URL and uploading the input stream to S3 with
the correct content type.
 */

package com.amazonaws.samples;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloaderUploader {

    public static void main(String[] args) {
        String bucketName = "s4059306-mybucket";  // Target bucket
        String jsonFilePath = "2025a1.json";

        // Initialize S3 client
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build();

        try {

            /*
              This code block reads the content of the json file, parses it as a JSON object, and retrieves
              the "songs" array. It then iterates through each song in the array, extracting the image URL
              (imgUrl) and artist name. To ensure consistency with a preassigned, case-sensitive link structure,
              it normalizes the artist name by removing whitespace and converting it to lowercase, creating a
              corresponding imageName with a ".jpg" extension. Subsequently, it downloads the image from the
              extracted URL and uploads it to the specified S3 bucket using the generated imageName. The process
              for each song is wrapped in a try-catch block to handle potential exceptions during file reading,
              JSON parsing, image downloading, or S3 uploading.

             */
            String content = new String(java.nio.file.Files.readAllBytes(new File(jsonFilePath).toPath()));
            JSONObject jsonObject = new JSONObject(content);
            JSONArray songs = jsonObject.getJSONArray("songs");

            for (int i = 0; i < songs.length(); i++) {
                JSONObject song = songs.getJSONObject(i);
                String imgUrl = song.getString("img_url");
                String artist = song.getString("artist");


                String imageName = artist.replaceAll("\\s+", "").toLowerCase() + ".jpg";

                InputStream imageStream = downloadImage(imgUrl);
                uploadToS3(s3Client, bucketName, imageName, imageStream);

                System.out.println("✅ Uploaded: " + imageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        return connection.getInputStream();
    }

    private static void uploadToS3(AmazonS3 s3Client, String bucketName, String imageName, InputStream imageStream) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, imageName, imageStream, metadata);
            s3Client.putObject(putRequest);
        } catch (Exception e) {
            System.err.println("❌ Failed to upload: " + imageName);
            e.printStackTrace();
        }
    }
}