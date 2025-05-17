/*
This servlet handles user registration by receiving user details (email, username, password)
via a POST request. It checks if the provided email already exists in the "Login" DynamoDB table. If the email
is unique, it adds a new user record to the table; otherwise, it informs the user that the email is already
registered. The servlet responds with a JSON object indicating the success or failure of the registration.
*/

package com.amazonaws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class RegisterServlet extends HttpServlet {

    // Establishing connection to DynamoDB using AWS SDK and local profile
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider("default"))
            .withRegion("us-east-1")
            .build();

    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final Table table = dynamoDB.getTable("Login");


    /*
        The doPost method in this servlet handles the user registration process. It reads the JSON data sent
        in the request body, extracting the user's email, username, and password. It then checks if the
        provided email already exists in the "Login" DynamoDB table. If the email is unique, a new user item
        is created and added to the table. Finally, a JSON response is sent back to the client indicating
        whether the registration was successful or if the email already exists.
    */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
        String requestBody = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(requestBody);
        String email = jsonNode.get("email").asText();
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();


        GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
        Item item = table.getItem(spec);

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        if (item != null) {
            // Email already exists
            out.print("{\"success\": false, \"message\": \"The email already exists\"}");
        } else {
            // Email is unique, store new user
            Item newUser = new Item()
                    .withPrimaryKey("email", email)
                    .withString("user_name", username)
                    .withString("password", password);
            table.putItem(new PutItemSpec().withItem(newUser));

            out.print("{\"success\": true, \"message\": \"Registration successful\"}");
        }
        out.flush();
    }
}
