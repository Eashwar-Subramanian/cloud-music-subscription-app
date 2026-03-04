# Cloud Music Subscription App (Java + AWS)

Full-stack coursework project: a subscription-based music web application with Java servlets and AWS service integration.

---

## Features
- User registration and login
- Song search (title/artist/year/album)
- Subscribe / unsubscribe flows
- Artist images served from S3 (presigned URL pattern)
- Subscription persistence via DynamoDB

---

## Repo structure
- `Backend/` — Java servlets + Jetty server app
- `Frontend/` — web UI assets
- `DynamoDB/` — DynamoDB-related components
- `S3/` — S3 utilities, including:
  - `CreateBucket2.java` (S3 bucket creation)
  - `ImageDownloaderUploader.java` (download images from URLs and upload to S3)
- `Instructions.txt` — setup/run notes
- `Lambda Functions.txt` — Lambda function notes

---

## Review request
Open an Issue titled: **Review: cloud-music-subscription-app**  
Feedback wanted: README clarity, structure, and what to highlight first for hiring review.
