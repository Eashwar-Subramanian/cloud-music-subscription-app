/*
This servlet, manages user subscriptions to music in the "subscriptions" DynamoDB table.
It handles both subscribing to and unsubscribing from songs via POST requests. Upon receiving a request with
user email, song details (title, artist, album, year), and an optional "action" parameter
("subscribe" or "unsubscribe"), it either adds a new subscription record or deletes an existing one. It also
includes logic to generate a unique song ID if not provided and checks if a subscription already exists before
performing the action. The servlet responds with a JSON object indicating the success or failure of the operation.
*/

package com.amazonaws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class SubscribeServlet extends HttpServlet {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider("default"))
            .withRegion("us-east-1")
            .build();

    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final Table subscriptionsTable = dynamoDB.getTable("subscriptions");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read the JSON request body
        Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
        String requestBody = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        System.out.println("📥 Received JSON: " + requestBody); // Debugging

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(requestBody);


        /*
          The doPost method in the RegisterServlet handles the user registration process. It reads the
          JSON data sent in the request body, extracting the user's email, username, and password. It then
          checks if the provided email already exists in the "Login" DynamoDB table using a GetItem operation.
          If the email is unique, a new user item is created and added to the table using a PutItem operation.
          Finally, a JSON response is sent back to the client indicating whether the registration was
          successful or if the email already exists.

         */
        String email = jsonNode.get("email").asText();
        String title = jsonNode.get("title").asText();
        String artist = jsonNode.get("artist").asText();
        String album = jsonNode.get("album").asText();
        String year = jsonNode.get("year").asText();
        String action = jsonNode.has("action") ? jsonNode.get("action").asText() : "subscribe"; // "subscribe" or "unsubscribe"

        String songId = jsonNode.has("song_id") ? jsonNode.get("song_id").asText() : generateSongId(title, artist, album);

        if (songId == null || songId.isEmpty()) {
            sendErrorResponse(resp, "Error: Song does not have an ID.");
            return;
        }

        System.out.println("Generated Song ID: " + songId); // Debugging

        if (action.equals("unsubscribe")) {
            unsubscribeSong(email, songId, resp);
        } else {
            subscribeSong(email, songId, title, artist, album, year, resp);
        }
    }

    // Handles song subscription
    private void subscribeSong(String email, String songId, String title, String artist, String album, String year, HttpServletResponse resp) throws IOException {
        if (isSongSubscribed(email, songId)) {
            sendErrorResponse(resp, "You have already subscribed to this song.");
            return;
        }

        try {
            /*
            Code adapted from AWS SDK for Java documentation on the DynamoDB Document API for putting an item:
            https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_PutItem.html
            */
            Item item = new Item()
                    .withPrimaryKey("email", email, "song_id", songId)
                    .withString("title", title)
                    .withString("artist", artist)
                    .withString("album", album)
                    .withString("year", year);

            subscriptionsTable.putItem(new PutItemSpec().withItem(item));

            sendSuccessResponse(resp, "Subscribed to " + title + " by " + artist);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error subscribing to song: " + e.getMessage());
        }
    }


    private void unsubscribeSong(String email, String songId, HttpServletResponse resp) throws IOException {
        if (!isSongSubscribed(email, songId)) {
            sendErrorResponse(resp, "You are not subscribed to this song.");
            return;
        }

        try {
            /*
            Code adapted from AWS SDK for Java documentation on the DynamoDB Document API for deleting an item:
            https://docs.aws.amazon.com/AmazonS3/latest/userguide/delete-objects.html
            */
            subscriptionsTable.deleteItem(new DeleteItemSpec().withPrimaryKey("email", email, "song_id", songId));

            sendSuccessResponse(resp, "Unsubscribed from the song.");
        } catch (Exception e) {
            sendErrorResponse(resp, "Error unsubscribing from song: " + e.getMessage());
        }
    }

    // Generates a unique song ID (standard Java string manipulation).
    private String generateSongId(String title, String artist, String album) {
        if (title == null || artist == null || album == null) {
            return null;
        }
        return (title + "_" + artist + "_" + album).replaceAll("\\s+", "").toLowerCase();
    }

    // Checks if a song is already subscribed
    private boolean isSongSubscribed(String email, String songId) {
        try {

            /*
             Code adapted from AWS SDK for Java documentation on the DynamoDB Document API for getting an item:
             https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/example_dynamodb_GetItem_section.html
            */
            Item existingItem = subscriptionsTable.getItem("email", email, "song_id", songId);
            return existingItem != null;
        } catch (Exception e) {
            System.err.println("Error checking subscription: " + e.getMessage());
            return false;
        }
    }


    private void sendErrorResponse(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print("{\"success\": false, \"message\": \"" + message + "\"}");
        out.flush();
    }


    private void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print("{\"success\": true, \"message\": \"" + message + "\"}");
        out.flush();
    }
}
