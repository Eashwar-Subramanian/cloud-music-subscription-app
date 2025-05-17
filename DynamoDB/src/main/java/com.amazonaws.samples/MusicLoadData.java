
/*
This Java program, uses the AWS SDK for Java and the Jackson library to read song data from a JSON file
named "2025a1.json". It connects to a DynamoDB table named "Music" in the specified AWS region. The program then
iterates through the "songs" array in the JSON file, extracting the title, artist, year, album, and image URL for
each song. For each song, it uses the DynamoDB Document API to insert a new item into the "Music" table, with "year"
and "title" as the primary key and the other attributes stored as strings. The program prints messages indicating the
success or failure of each item insertion. Finally, it closes the JSON parser.
 */

package com.amazonaws.samples;

import java.io.File;
import java.util.Iterator;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MusicLoadData {

    public static void main(String[] args) throws Exception {

        // Initialize DynamoDB client
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        // Getting the "music" table
        Table table = dynamoDB.getTable("Music");


        JsonParser parser = new JsonFactory().createParser(new File("2025a1.json"));
        JsonNode rootNode = new ObjectMapper().readTree(parser);


        // Extracting the "songs" array from the root node
        ArrayNode songsNode = (ArrayNode) rootNode.path("songs");

        /*
         This loop iterates through each songNode within the songsNode (an array of song objects parsed from the JSON
         file). Inside the loop, it extracts the "title", "artist", "year", "album", and "img_url" as string or integer
         values from the current songNode. It then attempts to insert a new item into the "Music" DynamoDB table using
         the putItem method. The primary key for each item is set using the "year" and "title" attributes, and the
         "artist", "album", and "image_url" are added as string attributes. A success message is printed upon
         successful insertion.
         */
        for (JsonNode songNode : songsNode) {
            // Extracting the attributes from the JSON data
            String title = songNode.path("title").asText();
            String artist = songNode.path("artist").asText();
            int year = songNode.path("year").asInt(); // Ensure this is an integer
            String album = songNode.path("album").asText();
            String imageUrl = songNode.path("img_url").asText();

            try {
                // Inserting data into DynamoDB music table
                table.putItem(new Item()
                        .withPrimaryKey("year", year, "title", title)
                        .withString("artist", artist)
                        .withString("album", album)
                        .withString("image_url", imageUrl));

                System.out.println("PutItem succeeded: " + year + " " + title);

            } catch (Exception e) {
                System.err.println("Unable to add song: " + year + " " + title);
                System.err.println(e.getMessage());
            }
        }
        parser.close();
    }
}
