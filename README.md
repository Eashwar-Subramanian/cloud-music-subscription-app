# Cloud Based Music Subscription Web Application
This project is a full-stack cloud-based music subscription web application developed as part of my **Cloud Computing course**. It showcases real-world integration of **AWS services**, **Java backend (Servlets)**, and **HTML/CSS/JavaScript frontend**, hosted on an **EC2 instance** with **Apache2 and Jetty**.

---

## 🚀 Features

- ✅ **User Authentication** (Login/Register)
- ✅ **Music Search & Query Interface**
- ✅ **Subscribe/Unsubscribe to Songs**
- ✅ **Fetch & Display Artist Images from AWS S3**
- ✅ **Dynamic Frontend with Real-Time Updates**
- ✅ **Presigned URL generation for image access**
- ✅ **Backend APIs using Java Servlets**
- ✅ **Deployed on EC2 with Apache2 (Reverse Proxy)**
- ✅ **Integration with AWS Services (DynamoDB, Lambda, S3, API Gateway)**

---

## 💻 Technologies Used

| Component | Technology |
|----------|------------|
| Backend  | Java Servlets (Jetty Server) |
| Frontend | HTML, CSS, JavaScript |
| Deployment | Apache2 on EC2 (Ubuntu), Jetty |
| AWS Services | S3, DynamoDB, Lambda, API Gateway |
| Other Tools | PuTTY, Git, nano, `nohup` command |

---

---

## 🔄 Backend API Endpoints (via Jetty & Apache2 Reverse Proxy)

| Endpoint | Description |
|---------|-------------|
| `/login` | Authenticates a user |
| `/Register` | Registers a new user |
| `/queryMusic` | Searches music by title/artist/year/album |
| `/subscribe` | Subscribes user to selected song |
| `/unsubscribe` | Unsubscribes song |
| `/fetchSubscriptions` | Fetches all subscriptions of a user |
| `/artistImage` | Retrieves artist image via signed S3 URL |

---

## 🌐 AWS Integration Details

- ✅ **S3** — Stores artist images (fetched via signed URL in `ArtistImageServlet`)
- ✅ **DynamoDB** — Stores user info and song subscriptions
- ✅ **Lambda + API Gateway** — Handles registration, subscription, and unsubscription via Python-based Lambda functions
- ✅ **EC2** — Hosts the entire application using Apache2 and Jetty

---

## 🧩 Reverse Proxy Configuration (Apache2 Virtual Host)

Configured in `/etc/apache2/sites-available/000-default.conf` to forward API requests from port **80 → 8080**:

```apache
<VirtualHost *:80>
    DocumentRoot /var/www/html
    DirectoryIndex login.html

    ProxyPass /login http://localhost:8080/login
    ProxyPassReverse /login http://localhost:8080/login
    ...
</VirtualHost>

