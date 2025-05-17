/*
This Java servlet retrieves the user's subscription details fro the DynamoDB table called subscriptions.
After receiving the GET request with an "email" parameter that we've set to be the primary key, it queries
the table to return the items for that particular email. The same process of returning the JSON response
is also done here, but this time it contains all of the susbcription information like title, artist,album
and song_id.
 */

package com.amazonaws;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

@WebServlet("/fetchSubscriptions")
public class FetchSubscriptionsServlet extends HttpServlet {

    // Name of the DynamoDB table to access which is 'subscriptions' table
    private static final String TABLE_NAME = "subscriptions";
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private Table table;

    /*
    Code adapted from AWS Java SDK examples and documentation:
    https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GettingStarted.Java.01.html
    */

    @Override
    public void init() throws ServletException {

        // Creates a DynamoDB client using default profile credentials and the us-east-1 region
        client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider("default"))
                .withRegion("us-east-1")
                .build();

        dynamoDB = new DynamoDB(client);

        // Gets a reference to the "subscriptions" table
        table = dynamoDB.getTable(TABLE_NAME);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (email == null || email.isEmpty()) {
            out.print("{\"error\":\"Missing email parameter\"}");
            return;
        }

        /*
        Code adapted from AWS Java SDK - Scanning a table:
        https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Scan.html
        */


        ItemCollection<ScanOutcome> items = table.scan(new ScanFilter("email").eq(email));
        Iterator<Item> iterator = items.iterator();


        /*
           This block scans the DynamoDB table for items matching the given email, then iterates through
           the results to build a JSON response manually using StringBuilder. For each subscription,
           it appends the song’s title, artist, album, year, and song_id in JSON format. The loop handles
           comma placement between JSON objects and ensures the response is correctly formatted before
           sending it to the client.
        */

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"subscriptions\":[");

        boolean first = true;
        while (iterator.hasNext()) {
            Item item = iterator.next();

            if (!first) {
                jsonBuilder.append(",");
            } else {
                first = false;
            }

            jsonBuilder.append("{")
                    .append("\"title\":\"").append(item.getString("title")).append("\",")
                    .append("\"artist\":\"").append(item.getString("artist")).append("\",")
                    .append("\"album\":\"").append(item.getString("album")).append("\",")
                    .append("\"year\":\"").append(item.getString("year")).append("\",")
                    .append("\"song_id\":\"").append(item.getString("song_id")).append("\"")
                    .append("}");
        }

        jsonBuilder.append("]}");

        out.print(jsonBuilder.toString());
        out.flush();
        System.out.println("Querying DynamoDB for user: " + email);
    }

    @Override
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }
}
