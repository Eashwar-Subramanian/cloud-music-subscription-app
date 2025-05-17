package com.amazonaws;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class DynamoDBManager {
    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    "https://dynamodb.us-east-1.amazonaws.com", "us-east-1"))
            .build();

    private static final DynamoDB dynamoDB = new DynamoDB(client);

    public static DynamoDB getDynamoDB()
    {
        return dynamoDB;
    }
}
