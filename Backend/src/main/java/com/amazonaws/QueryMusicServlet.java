/*
This servlet, handles POST requests to search the "Music" DynamoDB table based on
provided criteria like title, year, artist, and album. It prioritizes an efficient query using the
ArtistAlbumIndex Global Secondary Index if both artist and album are supplied. Otherwise, it performs a
scan with applied filters for the given search terms. The servlet then returns the matching music records
as a JSON array in the HTTP response.
*/



package com.amazonaws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QueryMusicServlet extends HttpServlet {
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider("default"))
            .withRegion("us-east-1")
            .build();
    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final Table musicTable = dynamoDB.getTable("Music");


    /*
     This method processes POST requests to query music data from the DynamoDB "Music" table. It parses
     the JSON request body to extract optional search parameters like title, year, artist, and album.
     If both artist and album are provided, it performs an efficient query using the ArtistAlbumIndex GSI.
     Otherwise, it builds a list of ScanFilters to perform a fallback scan. Matching records are collected
     as JSON strings and returned in a JSON array in the response.
     */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read request body
        Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
        String requestBody = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(requestBody);

        String title = jsonNode.has("title") ? jsonNode.get("title").asText() : "";
        String year = jsonNode.has("year") ? jsonNode.get("year").asText() : "";
        String artist = jsonNode.has("artist") ? jsonNode.get("artist").asText() : "";
        String album = jsonNode.has("album") ? jsonNode.get("album").asText() : "";

        List<String> results = new ArrayList<>();

        /*
         Code and logic adapted for the use of Global Secondary Index (GSI) for efficient querying when
         both artist and album are given
         https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html
        */

        try {

            if (!artist.isEmpty() && !album.isEmpty()) {
                com.amazonaws.services.dynamodbv2.document.Index gsi = musicTable.getIndex("ArtistAlbumIndex");

                com.amazonaws.services.dynamodbv2.document.spec.QuerySpec querySpec = new com.amazonaws.services.dynamodbv2.document.spec.QuerySpec()
                        .withHashKey("artist", artist)
                        .withRangeKeyCondition(new com.amazonaws.services.dynamodbv2.document.RangeKeyCondition("album").eq(album));

                ItemCollection<?> items = gsi.query(querySpec);
                items.forEach(item -> results.add(item.toJSON()));

                /*
                 Even though scanning a table is resource-intensive compared to querying,
                 there are many permutations of how title, album, artist, and year can be combined.
                 Therefore, we apply our learning by using Query only when both artist and album are provided,
                 and fallback to Scan for other combinations.
                */


            } else {


                /*
                 Code adapted and learned to use the scanning the full table with filters if GSI is not usable
                 https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Scan.html
                */

                List<ScanFilter> filters = new ArrayList<>();
                if (!title.isEmpty()) filters.add(new ScanFilter("title").eq(title));
                if (!year.isEmpty()) {
                    try {
                        int yearInt = Integer.parseInt(year);
                        filters.add(new ScanFilter("year").eq(yearInt));
                    } catch (NumberFormatException ignored) {}
                }
                if (!artist.isEmpty()) filters.add(new ScanFilter("artist").eq(artist));
                if (!album.isEmpty()) filters.add(new ScanFilter("album").eq(album));

                ScanSpec scanSpec = new ScanSpec();
                if (!filters.isEmpty()) scanSpec.withScanFilters(filters.toArray(new ScanFilter[0]));

                ItemCollection<?> items = musicTable.scan(scanSpec);
                items.forEach(item -> results.add(item.toJSON()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send results back
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print("{\"results\": " + results + "}");
        out.flush();
    }

}
