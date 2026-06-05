🚀 Log Monitoring System

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Kafka](https://img.shields.io/badge/Kafka-Event%20Driven-black)
![MySQL](https://img.shields.io/badge/MySQL-Database-orange)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%20RBAC-red)
![Maven](https://img.shields.io/badge/Maven-Build-blue)
![GitHub repo size](https://img.shields.io/github/repo-size/hruthkarsha25/log-monitoring-system)
![GitHub last commit](https://img.shields.io/github/last-commit/hruthkarsha25/log-monitoring-system)

A production-style backend system for capturing, processing, and monitoring application logs with secure authentication, role-based access control, and event-driven streaming using Kafka.

📌 Overview
The **Log Monitoring System** simulates how modern distributed backend systems handle logging for security, analytics, monitoring, and debugging purposes.
It captures user activities such as **registration, login, and API requests**, processes them securely, and stores them for auditing and analysis.

🏗️ System Architecture

User
 │
 ▼
Spring Boot REST APIs
 │
 ├── Spring Security (JWT + RBAC)
 │
 ├── Kafka Producer
 │        │
 │        ▼
 │     Kafka Topic
 │        │
 │        ▼
 │   Kafka Consumer
 │        │
 │        ├── Alert Engine (optional extension)
 │        │
 │        └── Log Storage Service
 │                 │
 │                 ▼
 │               MySQL

✨ Features
* 🔐 User Authentication (Register / Login)
* 🛡️ JWT-based Secure Access
* 👥 Role-Based Access Control (RBAC)
* 📜 API Audit Logging (all user actions tracked)
* 📊 Log Analytics & Statistics APIs
* ⚡ Event-driven log processing using Kafka
* 🔍 Filtering logs by user, endpoint, method, and timestamp
* 🧠 Secure password storage using BCrypt

🧰 Tech Stack
* ☕ Java 21
* 🌱 Spring Boot
* 🔐 Spring Security + JWT
* 🗄️ MySQL
* 📨 Apache Kafka
* 🔧 Maven
* 🧪 Postman
* 🐳 Docker + Docker Compose

🔐 Security
* Passwords encrypted using **BCrypt**
* JWT token-based authentication for secured endpoints
* Role-based authorization (**USER / ADMIN**)
* Unauthorized access attempts are logged separately

📡 API Endpoints

Authentication APIs

POST /auth/register     → Register new user
POST /auth/login        → Login and receive JWT token
POST /auth/refresh      → Refresh JWT token

Log APIs

GET /logs/all           → Fetch all logs (ADMIN only)
GET /logs/user/{email}  → Fetch logs for specific user
GET /logs/stats         → View usage statistics

🐳 Docker Deployment
The application is fully containerized with **Docker + Docker Compose** to ensure a consistent setup across environments.

Services included:

* Spring Boot Application
* Kafka + Zookeeper
* MySQL Database

▶️ Run the Project

  git clone https://github.com/hruthkarsha25/log-monitoring-system.git
  cd log-monitoring-system

⚙️ Configure Environment

Update docker-compose.yml with:

* MySQL database name
* MySQL username & password
* Kafka configuration

🐳 Start Application

  docker-compose up --build

⚙️ Local Setup (Without Docker)
1. Clone the repository

  git clone https://github.com/hruthkarsha25/log-monitoring-system.git
  cd log-monitoring-system

2. Configure database

  spring.datasource.url=jdbc:mysql://localhost:3306/log_monitoring_db
  spring.datasource.username=root
  spring.datasource.password=your_password

3. Start dependencies

* Kafka
* MySQL

4. Run application
  
  mvn spring-boot:run

📘 API Documentation (Swagger UI)
The project includes **Swagger UI** for interactive API testing.
🔗 Swagger URL
  
  http://localhost:8080/swagger-ui/index.html

📄 OpenAPI Docs

  http://localhost:8080/v3/api-docs

✨ Features

* Interactive API testing from the browser
* JWT authentication support
* Easy visualization of endpoints
* Developer-friendly debugging

💡 Project Highlights

* Built using **event-driven architecture (Kafka)**
* Secure authentication using **JWT + RBAC**
* Fully containerized using Docker

📈 Future Enhancements

* ELK Stack integration (Elasticsearch + Logstash + Kibana)
* Real-time log monitoring dashboard (React-based UI)
* Rate limiting per user (API protection)
* Distributed tracing for microservices expansion

 🧠 What this project demonstrates

* Backend system design thinking
* Security-first architecture (JWT + RBAC)
* Event-driven processing using Kafka
* Production-style logging and monitoring concepts
* Deployment readiness using Docker
