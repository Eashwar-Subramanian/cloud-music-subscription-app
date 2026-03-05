# Cloud Music Subscription App (Java + AWS)

Coursework full-stack project: a subscription-based music web application with Java servlets and AWS integration components.

## What’s in this repo
Top-level structure:
- `Frontend/` — HTML pages (login, register, main UI)
- `Backend/` — Java servlet backend (Jetty-based)
- `DynamoDB/` — DynamoDB components (registration/login persistence)
- `S3/` — S3 utilities (bucket creation + image download/upload)
- `Instructions.txt` — setup/run notes
- `Lambda Functions.txt` — Lambda code snippets used in the project workflow

## Features demonstrated (based on repo contents + instructions)
- User registration and login
- Music search (title/artist/year/album)
- Subscribe / unsubscribe flows
- Artist images stored/served from S3 (utility programs included)
- Subscription persistence via DynamoDB components

## Notable AWS SDK utilities included (inside `S3/`)
- `CreateBucket2.java` — creates an S3 bucket via AWS SDK for Java
- `ImageDownloaderUploader.java` — reads a JSON file, downloads images from URLs, and uploads them to S3

## Run / review approach
This is a multi-component coursework repo (frontend + backend + AWS services).  
For reviewers, the fastest way to evaluate is:

1) Read `Instructions.txt` (setup order and expected AWS prerequisites)
2) Scan the backend servlet code under `Backend/`
3) Scan AWS integration components under `DynamoDB/` and `S3/`
4) Check `Lambda Functions.txt` for the serverless snippets used during the project

## Feedback I want
Open a GitHub Issue titled: **Review: cloud-music-subscription-app** and tell me:
1) Whether the repo structure + instructions are clear enough to reproduce  
2) What to highlight first for a hiring manager (top 3 files/areas)  
3) Any confusing naming or missing “glue” that blocks understanding
