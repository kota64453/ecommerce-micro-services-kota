# 🛒 E-Commerce Microservices Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=for-the-badge&logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?style=for-the-badge&logo=mongodb)
![Redis](https://img.shields.io/badge/Redis-7.0-red?style=for-the-badge&logo=redis)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?style=for-the-badge&logo=rabbitmq)

A production-ready, fully containerized **E-Commerce backend** built with Spring Boot microservices, featuring JWT authentication, OTP verification, event-driven notifications, distributed caching, and API gateway routing.

</div>

---

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Services](#-services)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Authentication Flow](#-authentication-flow)
- [Event-Driven Communication](#-event-driven-communication)
- [Database Design](#-database-design)
- [Environment Variables](#-environment-variables)
- [Service Ports](#-service-ports)

---

## 🏗️ Architecture Overview

This project follows a **microservices architecture** where each service is independently deployable, has its own database, and communicates via REST (sync) or RabbitMQ (async).

```
                        ┌─────────────────────────────────────────┐
                        │           CLIENT (Postman / Browser)    │
                        └─────────────────┬───────────────────────┘
                                          │ HTTP :8080
                                          ▼
                        ┌─────────────────────────────────────────┐
                        │           API GATEWAY (:8080)           │
                        │  • JWT Authentication Filter            │
                        │  • Route to services via Eureka lb://   │
                        │  • Circuit Breaker (Resilience4j)       │
                        │  • Logging Filter                       │
                        └──────────────────┬──────────────────────┘
                                           │
               ┌───────────┬──────────────┼──────────────┬───────────┐
               ▼           ▼              ▼              ▼           ▼
        ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌──────────┐
        │  Auth    │ │  User    │ │  Product  │ │  Order   │ │ Payment  │
        │ :8081    │ │  :8082   │ │  :8083    │ │  :8084   │ │  :8085   │
        └────┬─────┘ └────┬─────┘ └─────┬─────┘ └────┬─────┘ └────┬─────┘
             │            │             │             │             │
             ▼            ▼             ▼             ▼             ▼
          MySQL         MySQL        MongoDB        MySQL         MySQL
          Redis         Redis                       Redis
          RabbitMQ                               RabbitMQ      RabbitMQ
               │                                      │             │
               └──────────────────┬───────────────────┘─────────────┘
                                  ▼
                    ┌─────────────────────────┐
                    │  Notification  :8088    │
                    │  • RabbitMQ Consumer    │
                    │  • Email via Gmail SMTP │
                    │  • Thymeleaf Templates  │
                    └─────────────────────────┘

        ┌──────────────────────────────────────────────────┐
        │         Eureka Service Registry (:8761)          │
        │  All services register and discover each other   │
        └──────────────────────────────────────────────────┘
```

---

## 🧩 Services

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| **API Gateway** | 8080 | Single entry point, JWT filter, routing, circuit breaker | — |
| **Eureka Server** | 8761 | Service registry & discovery | — |
| **Auth Service** | 8081 | Signup, Login, OTP, JWT tokens, password reset | MySQL + Redis |
| **User Service** | 8082 | User profiles, address management | MySQL + Redis |
| **Product Service** | 8083 | Product catalog, search, pagination | MongoDB |
| **Order Service** | 8084 | Cart (Redis), order placement & tracking | MySQL + Redis |
| **Payment Service** | 8085 | Payment processing, status tracking | MySQL |
| **Notification Service** | 8088 | Email notifications via RabbitMQ events | — |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 25 |
| Framework | Spring Boot 3.x |
| Service Registry | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Auth | JWT (JJWT), Spring Security |
| ORM | Spring Data JPA (Hibernate) |
| NoSQL ORM | Spring Data MongoDB |
| Caching | Spring Data Redis |
| Messaging | Spring AMQP (RabbitMQ) |
| Email | Spring Mail + Thymeleaf |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Mapping | MapStruct + ModelMapper |
| Build | Maven 3.9 |
| Containerization | Docker + Docker Compose |
| Databases | MySQL 8.0, MongoDB 7, Redis 7 |

---

## 📁 Project Structure

```
ecommerce-micro-services/
│
├── docker-compose.yml              # Full stack orchestration
│
├── api-gateway-service/            # Route everything, validate JWT
│   ├── src/main/java/
│   │   ├── filter/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── LoggingFilter.java
│   │   ├── config/
│   │   │   └── AuthorizationConfig.java
│   │   └── handler/
│   │       └── FallbackController.java
│   └── src/main/resources/
│       └── application.yaml        # Route definitions
│
├── eureka-service/                 # Service registry
│
├── auth-service/                   # Auth logic
│   └── src/main/java/com/ecommerce/auth/
│       ├── controller/AuthController.java
│       ├── service/AuthService.java
│       ├── redis/SignupRedisService.java
│       ├── producer/EventProducer.java
│       ├── util/JwtUtil.java
│       └── config/SecurityConfig.java
│
├── user-service/                   # User profiles
├── product-service/                # Product catalog
├── order-service/                  # Cart + Orders
├── payment-service/                # Payments
│
└── notification-service/           # Email dispatcher
    └── src/main/resources/templates/
        ├── welcome-email.html
        ├── otp-email.html
        ├── order-confirmation-email.html
        └── payment-success-email.html
```

---

## 🚀 Getting Started

### Prerequisites

- **Docker Desktop** (with WSL2 on Windows)
- **Git**

That's it. No Java or Maven installation needed — everything runs inside Docker.

### Clone & Run

```bash
# Clone the repository
git clone https://github.com/kota64453/ecommerce-micro-services-kota.git
cd ecommerce-micro-services-kota

# Start the entire stack
docker compose up -d --build
```

First build takes ~5–10 minutes (Maven downloads dependencies inside containers). Subsequent builds use Docker layer cache and are much faster.

### Verify Everything is Running

```bash
docker compose ps
```

Expected output — all containers should show `Up` or `healthy`:

```
NAME                   STATUS
api-gateway            Up
auth-service           Up
user-service           Up
product-service        Up
order-service          Up
payment-service        Up
notification-service   Up
eureka-service         Up (healthy)
ecommerce-mysql        Up (healthy)
ecommerce-mongodb      Up (healthy)
ecommerce-redis        Up (healthy)
ecommerce-rabbitmq     Up (healthy)
```

### Open the Eureka Dashboard

```
http://localhost:8761
```

You should see all services registered with status **UP**.

### Stop the Stack

```bash
docker compose down -v
```

---

## 📡 API Reference

All requests go through the **API Gateway at `http://localhost:8080`**.

### Auth Service — `/api/auth`

#### Sign Up
```http
POST /api/auth/signup
Content-Type: application/json

{
  "name": "Koushik",
  "email": "koushik@gmail.com",
  "password": "Password@123"
}
```

Response:
```json
{
  "success": true,
  "message": "OTP sent to your email. Please verify to complete signup.",
  "data": null
}
```

#### Verify OTP (Complete Signup)
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "koushik@gmail.com",
  "otp": "847291"
}
```

Response:
```json
{
  "success": true,
  "message": "Signup successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1...",
    "refreshToken": "eyJhbGciOiJIUzI1...",
    "userId": 1,
    "email": "koushik@gmail.com"
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "koushik@gmail.com",
  "password": "Password@123"
}
```

#### Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "koushik@gmail.com"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "koushik@gmail.com",
  "otp": "382910",
  "newPassword": "NewPass@456"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1..."
}
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <access_token>
X-User-Id: 1
```

---

### Product Service — `/api/products`

> These endpoints require a valid JWT token: `Authorization: Bearer <token>`

#### Create Product
```http
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Apple flagship smartphone",
  "price": 129999.00,
  "stock": 50,
  "category": "Electronics",
  "productCode": "APPL-IP15P"
}
```

#### Get All Products (Paginated)
```http
GET /api/products?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>
```

#### Search Products
```http
GET /api/products/search?keyword=iphone
Authorization: Bearer <token>
```

#### Get Product by ID
```http
GET /api/products/{id}
Authorization: Bearer <token>
```

#### Update Product
```http
PUT /api/products/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "price": 119999.00,
  "stock": 45
}
```

---

### Order Service — `/api/cart` & `/api/orders`

#### Add to Cart
```http
POST /api/cart/add
Authorization: Bearer <token>
X-User-Id: 1
Content-Type: application/json

{
  "productId": "64f3b2c1a2e4d5f6g7h8i9j0",
  "productName": "iPhone 15 Pro",
  "quantity": 2,
  "price": 129999.00
}
```

#### View Cart
```http
GET /api/cart
Authorization: Bearer <token>
X-User-Id: 1
```

#### Update Cart Item
```http
PUT /api/cart/update/{productId}?quantity=3
Authorization: Bearer <token>
X-User-Id: 1
```

#### Remove from Cart
```http
DELETE /api/cart/remove/{productId}
Authorization: Bearer <token>
X-User-Id: 1
```

#### Place Order
```http
POST /api/orders
Authorization: Bearer <token>
X-User-Id: 1
Content-Type: application/json

{
  "addressId": 1,
  "paymentMethod": "CARD"
}
```

---

## 🔐 Authentication Flow

The platform uses a **two-step signup flow** with OTP verification:

```
Client                  Auth Service              Redis              RabbitMQ         Notification
  │                          │                      │                   │                  │
  │── POST /signup ─────────>│                      │                   │                  │
  │                          │── store signup ──────>│                   │                  │
  │                          │   data + OTP in Redis │                   │                  │
  │                          │── publish OtpEvent ──────────────────────>│                  │
  │<─ 201 OTP sent ──────────│                      │                   │── send email ───>│
  │                          │                      │                   │                  │
  │── POST /verify-otp ─────>│                      │                   │                  │
  │                          │── get data from Redis>│                   │                  │
  │                          │── validate OTP        │                   │                  │
  │                          │── save user to MySQL  │                   │                  │
  │                          │── publish WelcomeEvent────────────────────>│                │
  │<─ 200 JWT tokens ────────│                      │                   │── send email ───>│
```

### JWT Token Validation in Gateway

```
Client ──── GET /api/products ──── Authorization: Bearer <token>
                                              │
                                    API Gateway JwtAuthenticationFilter
                                              │
                                    Extract + validate JWT signature
                                              │
                              ┌───────────────┴──────────────────┐
                        Token valid                         Token invalid
                              │                                   │
                    Forward request with                  Return 401 Unauthorized
                    X-User-Id header injected
                              │
                    Downstream service (product-service)
```

### JWT Util — Token Generation

```java
// JwtUtil.java (auth-service)
public String generateAccessToken(Long userId, String email) {
    return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("email", email)
            .setIssuer(issuer)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}
```

---

## 📨 Event-Driven Communication

The platform uses **RabbitMQ** for async communication between services. Auth/Order/Payment services publish events; Notification service consumes them and sends emails.

### Event Types

| Event | Publisher | Consumer | Trigger |
|-------|-----------|----------|---------|
| `OtpEvent` | Auth Service | Notification | User signup / resend OTP |
| `WelcomeEvent` | Auth Service | Notification | Successful signup |
| `ForgotPasswordEvent` | Auth Service | Notification | Forgot password request |
| `OrderCreatedEvent` | Order Service | Notification | Order placement |
| `PaymentSuccessEvent` | Payment Service | Notification | Payment confirmed |

### RabbitMQ Config

```java
// RabbitMQConfig.java (auth-service)
@Bean
public Queue otpQueue() {
    return new Queue("otp.queue", true);
}

@Bean
public Queue welcomeQueue() {
    return new Queue("welcome.queue", true);
}

@Bean
public DirectExchange authExchange() {
    return new DirectExchange("auth.exchange");
}
```

### Event Producer

```java
// EventProducer.java
public void publishOtpEvent(OtpEvent event) {
    rabbitTemplate.convertAndSend("auth.exchange", "otp.routing.key", event);
    log.info("Published OtpEvent for email: {}", event.getEmail());
}
```

### Notification Consumer

```java
// NotificationConsumer.java
@RabbitListener(queues = "otp.queue")
public void consumeOtpEvent(OtpEvent event) {
    log.info("Received OtpEvent for: {}", event.getEmail());
    emailService.sendOtpEmail(event.getEmail(), event.getOtp(), event.getName());
}

@RabbitListener(queues = "order.created.queue")
public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
    emailService.sendOrderConfirmationEmail(event);
}
```

### RabbitMQ Management UI

```
http://localhost:15672
Username: guest
Password: guest
```

---

## 🗄️ Database Design

### MySQL Databases (auto-created on startup)

| Database | Service | Tables |
|----------|---------|--------|
| `ecommerce_auth` | Auth Service | `users`, `refresh_tokens` |
| `ecommerce_users` | User Service | `user_profiles`, `addresses` |
| `ecommerce_orders` | Order Service | `orders`, `order_items` |
| `ecommerce_payments` | Payment Service | `payments` |

### MongoDB Collections

| Collection | Service | Description |
|------------|---------|-------------|
| `products` | Product Service | Product catalog with rich attributes |

### Redis Keys

| Pattern | Service | TTL | Description |
|---------|---------|-----|-------------|
| `signup:{email}` | Auth | 5 min | Pending signup + OTP |
| `cart:{userId}` | Order | Session | Shopping cart items |
| `token:blacklist:{token}` | Auth | Token TTL | Logged-out tokens |

---

## ⚙️ Environment Variables

| Variable | Service | Default | Description |
|----------|---------|---------|-------------|
| `SPRING_DATASOURCE_URL` | user, order, payment | — | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | user, order, payment | `root` | MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | user, order, payment | `root` | MySQL password |
| `SPRING_DATA_REDIS_HOST` | auth, user, order | `redis` | Redis hostname |
| `SPRING_RABBITMQ_HOST` | auth, order, payment, notification | `rabbitmq` | RabbitMQ hostname |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | all services | `http://eureka-service:8761/eureka/` | Eureka URL |
| `SPRING_MAIL_USERNAME` | notification | — | Gmail address |
| `SPRING_MAIL_PASSWORD` | notification | — | Gmail App Password |

---

## 🌐 Service Ports

| Service | Host Port | Container Port |
|---------|-----------|----------------|
| API Gateway | 8080 | 8080 |
| Auth Service | 8081 | 8081 |
| User Service | 8082 | 8082 |
| Product Service | 8083 | 8083 |
| Order Service | 8084 | 8084 |
| Payment Service | 8085 | 8085 |
| Notification Service | 8088 | 8088 |
| Eureka Server | 8761 | 8761 |
| MySQL | 3307 | 3306 |
| MongoDB | 27018 | 27017 |
| Redis | 6379 | 6379 |
| RabbitMQ AMQP | 5672 | 5672 |
| RabbitMQ UI | 15672 | 15672 |

---

## 🔧 Useful Commands

```bash
# View logs of a specific service
docker compose logs auth-service -f

# Restart a single service after code change
docker compose build auth-service
docker compose up -d auth-service

# Access MySQL shell
docker exec -it ecommerce-mysql mysql -u root -proot

# Check all databases
docker exec -it ecommerce-mysql mysql -u root -proot -e "SHOW DATABASES;"

# Access Redis CLI
docker exec -it ecommerce-redis redis-cli

# Check all Redis keys
docker exec -it ecommerce-redis redis-cli KEYS "*"

# View container resource usage
docker stats
```

---

## 👤 Author

**Koushik** — Information Technology Student, CMR University Bangalore  
GitHub: [@kota64453](https://github.com/kota64453)

---

<div align="center">
  Built with ☕ Java, 🐳 Docker, and lots of debugging
</div>
