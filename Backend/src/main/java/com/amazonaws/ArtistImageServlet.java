/*
This ArtistImageServlet is one among the other files that brings the code together? After importing
all the necessary packages, we've started building the public class called ArtistImageServlet. In short,
the basic functioning of this file is receive the artists image in a POST request and then generate
a pre-signed URL for the corresponding image stored in our S3 bucket. In the S3 bucket we've created, the
artists name is stored in lowercase charaters with no space and appended with ".jpg".
This is done to make it easier for tha anomality to be avoided. For example, some of the artist name is
like 'The Tallest Man on Earth'. But in the json file, the artist name is like 'The Tallest Man On Earth'
When we try to fetch the image, the preassigned url is mismatch because of the case sensitivity in the nam
when joined.
 */



package com.amazonaws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

public class ArtistImageServlet extends HttpServlet {

    /*
      Code adapted from Amazon S3( AWS SDK ) of Java documentation:
      https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-s3-client.html
      https://github.com/aws/aws-sdk-java/tree/master/src/samples/AmazonS3
    */
    private final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()

            .withCredentials(new ProfileCredentialsProvider("default"))
            .withRegion("us-east-1")
            .build();

    // The unique bucket name created as part of task 2 is used here
    private final String bucketName = "s4059306-mybucket";


     /*
       Code adapted from sitepoint:
       https://www.sitepoint.com/java-servlets-2/
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
        String requestBody = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        // Using Jackson ObjectMapper to parse JSON request body.
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(requestBody);


        String artist = jsonNode.get("artist").asText();

        // This way, it helps to avoid inconsistencies when trying to retrieve the correct image.
        String fileKey = artist.replaceAll("\\s+", "").toLowerCase() + ".jpg";
        String imageUrl;

        /*
        After conversion and appending th artisit name with a .jpg, an expiration date is set make that
        pre assigned link be valid only for a certain duration of time. We have done this, keeping in mind
        the severity of leaking credential information of the both S3 bucket and the aws. These  URLs provide
        secure access to private S3 objects without exposing the AWS credentials. The trial and error
        mhelps to generate and return an JSON object either with real or mock URLs.

        */
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 hour

            GeneratePresignedUrlRequest presignedRequest = new GeneratePresignedUrlRequest(bucketName, fileKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);

            URL signedUrl = s3Client.generatePresignedUrl(presignedRequest);
            imageUrl = signedUrl.toString();
        } catch (Exception e) {
            imageUrl = "https://via.placeholder.com/100";
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print("{\"artist\": \"" + artist + "\", \"image_url\": \"" + imageUrl + "\"}");
        out.flush();
    }
}