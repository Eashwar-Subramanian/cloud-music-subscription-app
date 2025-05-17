/*
In short, This servlet is used for mainly getting the user's saved subscribed songs, which is looked
for this information in the database table called 'subscriptions'. It requires the user's email address.
And sends the list of songs back in a format that web browsers understand (like a structured message).
 */


package com.amazonaws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

public class MainPageServlet extends HttpServlet {

    /*
      AWS SDK for Java documentation for creating a DynamoDB client:
      https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-dynamodb-client.html
      https://github.com/aws/aws-sdk-java/tree/master/src/samples/AmazonDynamoDB
    */

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()

            /*
               Code adapted from AWS SDK for ProfileCredentialsProvider:
               https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-file
             */

            .withCredentials(new ProfileCredentialsProvider("default"))
            .withRegion("us-east-1")
            .build();


    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final Table subscriptionsTable = dynamoDB.getTable("subscriptions");

    /*
        Code adapted from the documentation helping on Java Servlet API:
        https://jakarta.ee/specifications/servlet/5.0/jakarta-servlet-spec-5.0.html
     */


    /*
      This method handles GET requests by retrieving a user's subscriptions from the DynamoDB
      'subscriptions' table using their email as the key. It disables caching to ensure updated data
      is always returned, checks if the email parameter is valid, and queries the database for matching
      entries. The results are converted into a JSON array using Jackson's ObjectMapper, where each
      subscription's details are included if available. Finally, the JSON response is sent back to the client.
    */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies

        String email = request.getParameter("email");
        if (email == null || email.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().print("{\"error\": \"Missing email parameter\"}");
            return;
        }

         /*
           JSON object creation and response writing adapted from:
           https://github.com/FasterXML/jackson-databind
         */


        // Fetching subscriptions from DynamoDB
        ItemCollection<QueryOutcome> subscriptions = subscriptionsTable.query("email", email);

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode songsArray = objectMapper.createArrayNode();

        Iterator<Item> iterator = subscriptions.iterator();
        System.out.println("Fetching subscriptions for: " + email);

        /*

           This loop iterates over each item returned from the DynamoDB query. For every subscription
           record, it prints the full JSON for debugging, creates a new JSON object (songNode), and
           conditionally adds song details like title, artist, album, year, and song_id if they are present.
           Each constructed songNode is then added to the songsArray for the final response.

        */

        while (iterator.hasNext()) {
            Item item = iterator.next();
            System.out.println(item.toJSONPretty());
            ObjectNode songNode = objectMapper.createObjectNode();
            if (item.hasAttribute("title")) songNode.put("title", item.getString("title"));
            if (item.hasAttribute("artist")) songNode.put("artist", item.getString("artist"));
            if (item.hasAttribute("album")) songNode.put("album", item.getString("album"));
            if (item.hasAttribute("year")) songNode.put("year", item.getString("year"));
            if (item.hasAttribute("song_id")) songNode.put("song_id", item.getString("song_id")); // <-- ADD THIS
            songsArray.add(songNode);
        }


        // Sending JSON response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(songsArray));
        out.flush();
        System.out.println("Fetching subscriptions for: " + email);

    }
}
