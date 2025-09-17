# Cloud-Based Music Subscription Web Application
**Full-stack music platform with AWS integration and subscription management**

## 🎯 Application Overview
Complete web-based music subscription service demonstrating cloud architecture integration, user management, and AWS services coordination in a production-like environment.

## ✅ Core Features
- **User Authentication:** Login and registration system
- **Music Discovery:** Search functionality by title, artist, year, album
- **Subscription Management:** Subscribe/unsubscribe to songs
- **Media Integration:** Artist image display via AWS S3
- **Real-time Updates:** Dynamic frontend with live data refresh

## 🏗️ Architecture Components
**Frontend:**
- HTML/CSS/JavaScript interface
- Dynamic content updates
- Responsive design implementation

**Backend:**
- Java Servlets on Jetty server
- RESTful API endpoints
- Apache2 reverse proxy configuration

**AWS Integration:**
- **S3:** Artist image storage with presigned URL access
- **DynamoDB:** User data and subscription storage
- **Lambda:** Serverless functions for subscription logic
- **EC2:** Application hosting on Ubuntu
- **API Gateway:** Lambda function exposure

## 🔧 Technical Implementation
**Deployment Environment:**
- **Server:** EC2 Ubuntu instance
- **Web Server:** Apache2 with reverse proxy (port 80 → 8080)
- **Application Server:** Jetty for servlet container
- **Process Management:** nohup for persistent service

**API Endpoints:**
- `/login` - User authentication
- `/Register` - New user registration  
- `/queryMusic` - Music search functionality
- `/subscribe` - Song subscription handling
- `/unsubscribe` - Subscription removal
- `/fetchSubscriptions` - User subscription retrieval
- `/artistImage` - S3 image access via signed URLs

## 🌐 AWS Services Integration
**Amazon S3:**
- Artist image storage and retrieval
- Presigned URL generation for secure access
- Media content management

**DynamoDB:**
- NoSQL database for user profiles
- Subscription tracking and management
- Scalable data storage solution

**Lambda Functions:**
- Python-based serverless subscription logic
- Event-driven subscription processing
- Cost-effective compute for specific operations

## 🚀 Deployment Configuration
**Apache2 Virtual Host Setup:**
```apache
<VirtualHost *:80>
    DocumentRoot /var/www/html
    DirectoryIndex login.html
    ProxyPass /login http://localhost:8080/login
    ProxyPassReverse /login http://localhost:8080/login
    # Additional proxy configurations for all endpoints
</VirtualHost>
