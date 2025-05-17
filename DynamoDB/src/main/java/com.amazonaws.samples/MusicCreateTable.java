// Copyright 2012-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// Licensed under the Apache License, Version 2.0.

/*
This Java program, uses the AWS SDK for Java to create a DynamoDB table named "Music". The table's primary key
consists of a partition key named "year" (a Number) and a sort key named "title" (a String). The table is provisioned
with an initial read and write capacity of 10 units each. The program outputs status messages indicating whether the
table creation was successful.
 */

/*
Code adapted from the Cloud Computing week 4 practicals
 */

package com.amazonaws.samples;

import java.util.Arrays;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

public class MusicCreateTable {

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = "Music";

        try {
            System.out.println("Attempting to create table; please wait...");


            /*
              This code snippet uses the DynamoDB Document API to create a new table. It defines the table named by
              the tableName variable, sets the primary key schema with "year" as the partition key (HASH) and "title"
              as the sort key (RANGE), and specifies the attribute types for these keys as Number (N) and String (S)
              respectively. It also sets the initial provisioned read and write capacity to 10 units each. Finally,
              it waits for the table to become active and then prints a success message along with the table's current
              status.
             */

            /*
              Code adapted from AWS SDK of create table documentation:
              https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_CreateTable.html

             */
            Table table = dynamoDB.createTable(
                    tableName,
                    Arrays.asList(
                            new KeySchemaElement("year", KeyType.HASH),  // Partition Key
                            new KeySchemaElement("title", KeyType.RANGE) // Sort Key
                    ),
                    Arrays.asList(
                            new AttributeDefinition("year", ScalarAttributeType.N),
                            new AttributeDefinition("title", ScalarAttributeType.S)
                    ),
                    new ProvisionedThroughput(10L, 10L));


            /*
              Code learned from the AWS SDK for JAVA documentation:
              https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/model/TableDescription.htm
             */
            table.waitForActive();
            System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create table: ");
            System.err.println(e.getMessage());
        }

    }
}