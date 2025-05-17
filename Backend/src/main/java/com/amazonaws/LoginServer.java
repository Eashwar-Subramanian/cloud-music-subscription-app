/*
This class sets up and runs a Jetty HTTP server on port 8080. It configures various servlets
for handling different API endpoints, including login, registration, querying music, subscribing/unsubscribing,
fetching artist images, and retrieving user subscriptions. It also includes a CORS filter to handle
cross-origin requests. The LoginServlet within this class handles user authentication by verifying provided
email and password against the "Login" DynamoDB table.
*/

package com.amazonaws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class LoginServer {

    // Entry point for the server
    public static void main(String[] args) throws Exception {
        // Creates a Jetty HTTP server instance on port 8080 (standard web server port).
        Server server = new Server(8080);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        // Adds a CORS filter to allow cross-origin requests (for frontend to communicate with backend).
        handler.addFilterWithMapping(new FilterHolder(new CORSFilter()), "/*", null);


        handler.addServletWithMapping(new ServletHolder(new LoginServlet()), "/login");
        handler.addServletWithMapping(new ServletHolder(new RegisterServlet()), "/Register");
        handler.addServletWithMapping(new ServletHolder(new QueryMusicServlet()), "/queryMusic");
        handler.addServletWithMapping(new ServletHolder(new SubscribeServlet()), "/subscribe");
        handler.addServletWithMapping(new ServletHolder(new ArtistImageServlet()), "/artistImage");
        handler.addServletWithMapping(new ServletHolder(new FetchSubscriptionsServlet()), "/fetchSubscriptions");


        server.start();
        System.out.println("✅ Server started on port 8080");
        server.join();
    }


    public static class LoginServlet extends HttpServlet {

        private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider("default"))  // use local creds
                .withRegion("us-east-1")
                .build();

        private final DynamoDB dynamoDB = new DynamoDB(client);
        private final Table table = dynamoDB.getTable("Login"); // DynamoDB table name


        /*
        The doPost method in the LoginServlet handles incoming HTTP POST requests to the /login endpoint.
        It reads the JSON request body, extracts the 'email' and 'password' parameters, and attempts to
        authenticate the user against the "Login" DynamoDB table. It retrieves the user item based on the
        provided email and then compares the stored password with the provided password. Based on the
        authentication result, it sends a JSON response indicating success (with username and email)
        or failure (due to invalid credentials).
        */

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // Read JSON request
            Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
            String requestBody = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            String email = jsonNode.get("email").asText();
            String password = jsonNode.get("password").asText();

            System.out.println("🔐 Login attempt by: " + email);


            GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
            Item item = table.getItem(spec);

            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();

            // Check credentials
            if (item != null) {
                if (item.getString("password").equals(password)) {
                    String username = item.getString("user_name");
                    out.print("{\"success\": true, \"message\": \"Login successful\", \"username\": \"" + username + "\", \"email\": \"" + email + "\"}");
                } else {
                    out.print("{\"success\": false, \"message\": \"Invalid email or password\"}");
                }
            }
            else {
                out.print("{\"success\": false, \"message\": \"Invalid email or password\"}");
            }
            out.flush();
        }
    }
}