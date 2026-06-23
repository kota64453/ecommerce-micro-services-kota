# 📚 Complete Pin-to-Pin Explanation
# E-Commerce Microservices Project

---

# PART 1: WHAT IS EVERYTHING — CONCEPTS EXPLAINED FROM SCRATCH

---

## 1. What is a Monolith vs Microservices?

### Monolith (the OLD way)
Imagine you build one big Spring Boot project. All your code — login, products, orders, payments — is inside ONE project, ONE jar file, deployed on ONE server.

**Problem:** If your payment code has a bug, the ENTIRE application goes down. If 1000 people visit your product page, you can't scale just the product part — you have to scale everything.

### Microservices (the NEW way — what THIS project uses)
You split your application into **small, independent services**. Each service:
- Has its OWN codebase (own Maven project, own `pom.xml`)
- Has its OWN database
- Runs in its OWN Docker container
- Can be deployed, scaled, and updated independently

```
MONOLITH                          MICROSERVICES
┌─────────────────────┐           ┌──────────┐  ┌──────────┐
│                     │           │  Auth    │  │ Product  │
│  Login + Products   │    vs     │ Service  │  │ Service  │
│  + Orders           │           └──────────┘  └──────────┘
│  + Payments         │           ┌──────────┐  ┌──────────┐
│  + Notifications    │           │  Order   │  │ Payment  │
│                     │           │ Service  │  │ Service  │
└─────────────────────┘           └──────────┘  └──────────┘
ONE jar = ONE failure point        Each is independent
```

**In this project, the microservices are:**
1. `eureka-service` — Service Registry
2. `api-gateway-service` — Entry point / router
3. `auth-service` — Login, signup, JWT tokens
4. `user-service` — User profiles, addresses, wishlist
5. `product-service` — Product catalog
6. `order-service` — Cart and orders
7. `payment-service` — Payment processing
8. `notification-service` — Emails

---

## 2. What is Eureka Service?

**Think of it like a phone directory** for your microservices.

When `auth-service` starts, it "registers" itself with Eureka saying:
> "Hi, I am AUTH-SERVICE and I'm running at IP 172.18.0.5, port 8081"

When `api-gateway` wants to forward a request to `auth-service`, it asks Eureka:
> "Where is AUTH-SERVICE right now?"

Eureka replies:
> "It's at 172.18.0.5:8081"

This is called **Service Discovery**. Without Eureka, you'd have to hardcode IPs everywhere, which breaks the moment a container restarts with a new IP.

**In this project:**
```yaml
# Every microservice has this in application.yml:
eureka:
  client:
    service-url:
      defaultZone: http://eureka-service:8761/eureka/
```

This tells every service: "Register yourself with Eureka at this URL."

**The Eureka server itself is tiny:**
```java
@SpringBootApplication
@EnableEurekaServer   // ← This ONE annotation makes it a Eureka server
public class EurekaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServiceApplication.class, args);
    }
}
```

**Eureka Dashboard:** Open `http://localhost:8761` to see all registered services.

---

## 3. What is API Gateway?

**Think of it like a security guard + receptionist at the entrance of a building.**

Every request from Postman/browser first hits the API Gateway at port `8080`. The gateway:
1. Checks your JWT token (authentication)
2. Checks if you have permission (authorization)
3. Forwards the request to the correct microservice
4. Returns the response back to you

```
You → POST /api/auth/signup → Gateway → forwards to → auth-service:8081
You → GET /api/products    → Gateway → forwards to → product-service:8083
You → POST /api/orders     → Gateway → forwards to → order-service:8084
```

**Without a gateway**, you'd need to know the port of every service and call them directly. With a gateway, you only need to know port `8080`.

**Route Configuration (application.yaml in api-gateway-service):**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE      # lb:// means "load balanced via Eureka"
          predicates:
            - Path=/api/auth/**       # any URL starting with /api/auth goes here
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/products/**
```

`lb://AUTH-SERVICE` means: "Ask Eureka where AUTH-SERVICE is, then send it there."

---

## 4. What is Docker and Docker Compose?

### Docker
**Think of Docker like a shipping container for software.** Just like a shipping container works the same whether it's on a ship, truck, or train — a Docker container works the same whether it's on your laptop, a server in Mumbai, or AWS in the USA.

A **Docker image** is like a blueprint. A **Docker container** is the running instance.

**Dockerfile** (each service has one) — tells Docker how to build the image:
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build   # Start with Maven + Java 17
WORKDIR /app                                  # Set working directory
COPY pom.xml .                               # Copy pom.xml first
COPY src ./src                               # Copy source code
RUN mvn clean package -DskipTests -q        # Build the jar

FROM eclipse-temurin:17-jre                  # Use lightweight runtime image
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar  # Copy built jar
ENTRYPOINT ["java", "-jar", "app.jar"]       # Run it
```

### Docker Compose
**Think of Docker Compose like a conductor for an orchestra.** Instead of starting 12 instruments (containers) one by one, Docker Compose starts all of them together in the right order.

The `docker-compose.yml` file defines:
- Which containers to start
- Their environment variables
- Their port mappings (host:container)
- Their dependencies (start MySQL before auth-service)
- Health checks (don't start auth-service until MySQL is ready)

```yaml
# Example from your docker-compose.yml:
auth-service:
  build:
    context: ./auth-service          # Use Dockerfile in this folder
  container_name: auth-service
  ports:
    - "8081:8081"                    # host_port:container_port
  environment:
    SPRING_DATA_REDIS_HOST: redis    # Tells Spring where Redis is
    SPRING_RABBITMQ_HOST: rabbitmq   # Tells Spring where RabbitMQ is
  depends_on:
    mysql:
      condition: service_healthy     # Wait until MySQL passes health check
    redis:
      condition: service_healthy
```

---

## 5. What is MySQL?

**MySQL is a relational database** — data stored in tables with rows and columns, like Excel.

**In this project, MySQL stores:**

| Database | Tables | What's stored |
|----------|--------|---------------|
| `ecommerce_users` | `user_credentials`, `user_profiles`, `addresses`, `refresh_tokens`, `wishlist_items` | User accounts, passwords |
| `ecommerce_orders` | `orders`, `order_items` | Order records |
| `ecommerce_payments` | `payments` | Payment records |

**Spring Boot creates the tables automatically** using JPA/Hibernate — you never write SQL `CREATE TABLE` manually.

**How?** Every class annotated with `@Entity` becomes a table:
```java
@Entity
@Table(name = "user_credentials")   // creates table named "user_credentials"
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment id
    private Long id;

    @Column(nullable = false, unique = true)  // NOT NULL, UNIQUE constraint
    private String email;
}
```

---

## 6. What is MongoDB?

**MongoDB is a NoSQL database** — data stored as JSON-like documents, NOT in tables.

**Why does product-service use MongoDB instead of MySQL?**

Products have **flexible, varying attributes**. A phone has RAM and storage. A shirt has size and color. A book has author and ISBN. These don't fit well in a fixed-column table. MongoDB lets each product document have different fields.

```json
// MongoDB product document
{
  "_id": "64f3b2c1a2e4d5f6g7h8i9j0",
  "name": "iPhone 15 Pro",
  "price": 129999.00,
  "category": "Electronics",
  "specifications": {
    "RAM": "8GB",
    "Storage": "256GB",
    "Camera": "48MP"
  }
}
```

**In Spring Boot, MongoDB entity uses `@Document` instead of `@Entity`:**
```java
@Document(collection = "products")  // collection = MongoDB's version of "table"
public class Product {
    @Id
    private String id;           // String, not Long! MongoDB uses string IDs

    @Indexed(unique = true)      // Creates a MongoDB index
    private String productCode;
}
```

---

## 7. What is Redis?

**Redis is an in-memory key-value store** — like a super-fast temporary database. Data is stored in RAM, not on disk, so reads/writes happen in microseconds.

**In this project, Redis is used for TWO things:**

### 1. Storing OTPs temporarily (auth-service)
When user signs up, OTP is stored in Redis with a 5-minute expiry:
```java
// Key: "OTP:koushik@gmail.com"   Value: "847291"   TTL: 5 minutes
redisTemplate.opsForValue().set("OTP:" + email, otp, 5, TimeUnit.MINUTES);
```
After 5 minutes, Redis **automatically deletes it**. No cleanup code needed.

### 2. Storing shopping cart (order-service)
Cart is stored in Redis per user:
```java
// Key: "CART:1"   Value: JSON array of cart items   TTL: 24 hours
// Key: "CART:2"   Value: JSON array of cart items   TTL: 24 hours
```
The cart expires after 24 hours of inactivity automatically.

**Why not just use MySQL for OTP and cart?**
- OTPs need to expire automatically — Redis TTL does this natively
- Cart data is temporary and accessed very frequently — Redis is 100x faster than MySQL
- No need for cleanup jobs or scheduled tasks

---

## 8. What is RabbitMQ?

**RabbitMQ is a message broker** — like a post office between services. It allows services to communicate **without calling each other directly**.

**The problem it solves:** When user signs up, auth-service needs to send a welcome email. There are two ways:

**Way 1 (bad) — Direct HTTP call:**
```
auth-service → HTTP call → notification-service → send email
```
If notification-service is down, auth-service fails too. The signup breaks.

**Way 2 (good) — Via RabbitMQ message:**
```
auth-service → publish message to RabbitMQ → (notification-service picks it up when ready)
```
Even if notification-service is down, the message waits in the queue. When it comes back up, it processes all pending messages.

### RabbitMQ Concepts:

**Exchange** — receives messages from publishers and routes them to queues based on routing keys.
```java
@Bean
public TopicExchange notificationExchange() {
    return new TopicExchange("notification.exchange");
}
```

**Queue** — holds messages waiting to be consumed.
```java
@Bean
public Queue otpQueue() {
    return new Queue("otp.queue", true);  // true = durable (survives restart)
}
```

**Binding** — connects an exchange to a queue via a routing key.
```java
@Bean
public Binding otpBinding() {
    return BindingBuilder
        .bind(otpQueue())                      // bind this queue
        .to(notificationExchange())            // to this exchange
        .with("otp.send");                     // when routing key matches "otp.send"
}
```

**Message flow in this project:**
```
auth-service (PRODUCER)
    │
    │  rabbitTemplate.convertAndSend("notification.exchange", "otp.send", otpEvent)
    ▼
RabbitMQ
    │  Routes message with key "otp.send" to "otp.queue"
    ▼
notification-service (CONSUMER)
    │  @RabbitListener(queues = "otp.queue")
    │  public void handleOtpEvent(OtpEvent event) { ... }
    ▼
    Sends email
```

---

## 9. What is Maven?

**Maven is a build tool** — it manages your project's dependencies (JAR files) and builds your application.

**`pom.xml`** = Maven's config file. You declare what libraries you need:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- Maven automatically downloads this JAR from Maven Central -->
</dependency>
```

Maven downloads the JAR files from the internet and puts them in your project's classpath. You never manually download JAR files.

**Common Maven commands:**
```bash
mvn clean package          # Delete old build + compile + create JAR
mvn clean package -DskipTests  # Same but skip tests (used in Dockerfile)
mvn dependency:go-offline  # Download all deps for offline use
```

---

# PART 2: THE PACKAGE STRUCTURE — WHAT EACH PACKAGE MEANS

---

## The Standard Package Layout (used in EVERY service)

```
com.ecommerce.auth/
│
├── controller/          ← HTTP layer — receives requests, sends responses
│   └── AuthController.java
│
├── service/             ← Business logic layer — the brain
│   └── AuthService.java
│
├── repository/          ← Database layer — talks to MySQL/MongoDB
│   └── UserRepository.java
│
├── entity/              ← Database model — maps to a DB table/document
│   └── User.java
│
├── dto/                 ← Data Transfer Objects — what you send/receive in APIs
│   ├── SignupRequest.java
│   └── AuthResponse.java
│
├── mapper/              ← Converts Entity ↔ DTO using MapStruct
│   └── UserMapper.java
│
├── config/              ← Configuration classes
│   ├── SecurityConfig.java
│   ├── RabbitMQConfig.java
│   └── RedisConfig.java
│
├── exception/           ← Custom exceptions + global error handler
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   └── ResourceNotFoundException.java
│
├── event/               ← RabbitMQ event objects (messages)
│   ├── OtpEvent.java
│   └── WelcomeEvent.java
│
├── producer/            ← Publishes messages to RabbitMQ
│   └── EventProducer.java
│
├── redis/               ← Redis operations
│   └── SignupRedisService.java
│
├── util/                ← Utility classes (JWT, OTP generation)
│   ├── JwtUtil.java
│   └── OtpUtil.java
│
└── client/              ← Feign clients (HTTP calls to other services)
    └── UserClient.java
```

---

# PART 3: THE REQUEST FLOW — HOW CODE EXECUTES

---

## The Request Pipeline (the MOST important concept)

**Every request in Spring Boot follows this exact path:**

```
HTTP Request
     │
     ▼
Controller (@RestController)
     │  receives the HTTP request
     │  validates input with @Valid
     │
     ▼
Service (@Service)
     │  contains all business logic
     │  calls repository, redis, rabbitmq
     │
     ▼
Repository (@Repository / JpaRepository)
     │  executes SQL/MongoDB queries
     │
     ▼
Database (MySQL / MongoDB)
     │
     ▼ (reverse path)
Service builds response
     │
     ▼
Controller wraps in ResponseEntity
     │
     ▼
HTTP Response back to client
```

---

# PART 4: AUTH SERVICE — EVERY CLASS EXPLAINED

---

## 4.1 SignupRequest.java (DTO)

```java
@Data           // Lombok: generates getters, setters, toString, equals
@NoArgsConstructor  // Lombok: generates empty constructor
@AllArgsConstructor // Lombok: generates constructor with all fields
public class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[A-Za-z]+(?: [A-Za-z]+)*$")
    private String name;

    @NotBlank
    @Email(message = "Invalid email format")  // validates email format
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")          // exactly 10 digits
    private String phone;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])...")
    // Password must have: uppercase, lowercase, digit, special char
    private String password;
}
```

**What is a DTO?**
DTO = Data Transfer Object. It's a simple class used to transfer data between the client and server. It's NOT stored in the database. The client sends `SignupRequest` JSON, Spring Boot deserializes it into this class.

**What does `@Valid` do?**
When controller has `@Valid @RequestBody SignupRequest request`, Spring runs all the validation annotations (`@NotBlank`, `@Email`, etc.) before the method executes. If validation fails, it automatically returns a 400 Bad Request.

---

## 4.2 AuthController.java

```java
@RestController             // = @Controller + @ResponseBody
                            // Every method returns JSON automatically
@RequestMapping("/api/auth") // Base URL for all endpoints in this class
@RequiredArgsConstructor    // Lombok: creates constructor for all final fields
@Slf4j                      // Lombok: creates log variable for logging
public class AuthController {

    private final AuthService authService;  // Injected by Spring (Dependency Injection)

    @PostMapping("/signup")  // POST /api/auth/signup
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest request) {
        //  ↑ validate input  ↑ parse JSON body

        ApiResponse<Void> response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // Returns HTTP 201 Created with the response body
    }
}
```

**What is `@RestController`?**
It tells Spring: "Every method in this class returns JSON data, not an HTML page."

**What is `ResponseEntity`?**
It lets you control the HTTP status code AND the response body. `ResponseEntity.ok(data)` = 200 OK. `ResponseEntity.status(201).body(data)` = 201 Created.

**What is `@RequiredArgsConstructor`?**
Lombok generates this constructor:
```java
public AuthController(AuthService authService) {
    this.authService = authService;
}
```
Spring sees this constructor and automatically injects `AuthService`. This is **Constructor Injection** — the recommended way to inject dependencies.

---

## 4.3 ApiResponse.java (Generic Response Wrapper)

```java
@Data
@Builder
public class ApiResponse<T> {       // <T> = generic type, can be anything
    private boolean success;
    private String message;
    private T data;                 // could be AuthResponse, UserDto, List<Products>, etc.
    private LocalDateTime timestamp;

    // Static factory methods for convenience:
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }
}
```

**Why wrap every response in `ApiResponse`?**
So the client always gets a consistent structure:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```
Without this wrapper, each endpoint returns different JSON structures, making it harder for the frontend to handle responses.

---

## 4.4 AuthService.java — The Brain

This is the most complex class. Let's go method by method.

### `signup()` method
```java
public ApiResponse<Void> signup(SignupRequest request) {

    // Step 1: Check if email already exists via Feign call to user-service
    ApiResponse<Boolean> emailCheck = userClient.checkEmailExists(request.getEmail());
    if (emailCheck.getData()) {
        throw new BusinessException("Email already registered");
    }

    // Step 2: Generate a 6-digit OTP
    String otp = otpUtil.generateOtp();

    // Step 3: Store OTP + signup data in Redis with 5-min TTL
    signupRedisService.storeOtp(request.getEmail(), otp);

    // Step 4: Publish OTP event to RabbitMQ
    // (notification-service picks it up and sends the email)
    OtpEvent otpEvent = OtpEvent.builder()
            .email(request.getEmail())
            .otp(otp)
            .name(request.getName())
            .build();
    eventProducer.publishOtpEvent(otpEvent);

    // Step 5: Return success (don't create user yet — wait for OTP verification)
    return ApiResponse.success("OTP sent to your email.");
}
```

**Why not create the user immediately on signup?**
Because you don't know if the email is real. Only after the user verifies the OTP do you know the email is valid. This is called **Two-Factor Signup**.

### `verifyOtp()` method
```java
public ApiResponse<AuthResponse> verifyOtp(VerifyOtpRequest request) {

    // Step 1: Validate OTP from Redis
    boolean isValid = signupRedisService.validateOtp(request.getEmail(), request.getOtp());
    if (!isValid) throw new BusinessException("Invalid or expired OTP");

    // Step 2: Get the original signup data from Redis
    SignupRequest signupRequest = signupRedisService.getSignupRequest(request.getEmail());

    // Step 3: Create user in user-service via Feign call
    CreateUserCredentialRequest createRequest = CreateUserCredentialRequest.builder()
            .email(signupRequest.getEmail())
            .password(passwordEncoder.encode(signupRequest.getPassword()))  // HASH password!
            .name(signupRequest.getName())
            .role("ROLE_USER")
            .build();
    UserServiceUserDto userDto = userClient.registerUser(createRequest).getData();

    // Step 4: Generate JWT tokens
    String accessToken = jwtUtil.generateAccessToken(userDto);
    String refreshToken = jwtUtil.generateRefreshToken(userDto);

    // Step 5: Save refresh token to user-service database
    saveRefreshToken(refreshToken, userDto);

    // Step 6: Publish welcome event (sends welcome email)
    eventProducer.publishWelcomeEvent(WelcomeEvent.builder()
            .email(userDto.getEmail()).name(userDto.getName()).build());

    // Step 7: Clean up Redis
    signupRedisService.deleteSignupData(request.getEmail());
    signupRedisService.deleteOtp(request.getEmail());

    // Step 8: Return JWT tokens
    return ApiResponse.success("Registration successful", buildAuthResponse(accessToken, refreshToken, userDto));
}
```

---

## 4.5 JwtUtil.java — JWT Token Generation

**What is a JWT?**
JWT = JSON Web Token. It's a secure string that proves who you are. When you login, the server gives you a JWT. For every subsequent request, you send this JWT in the header. The server validates it without needing to look up your session in a database.

**JWT structure:** `header.payload.signature`
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJrb3VzaGlrQGdtYWlsLmNvbSJ9.abc123xyz
     ↑ header              ↑ payload (base64 encoded)                             ↑ signature
```

```java
public String generateAccessToken(UserServiceUserDto user) {

    return JWT.create()
            .withIssuer(issuer)                         // who created this token
            .withSubject(user.getId().toString())       // who this token is for
            .withClaim("userId", user.getId().toString())
            .withClaim("email", user.getEmail())
            .withClaim("name", user.getName())
            .withArrayClaim("roles", new String[]{user.getRole()})
            .withIssuedAt(new Date())                   // when created
            .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))  // expires in 24h
            .sign(algorithm);                           // sign with secret key (HMAC256)
}
```

**Token validation:**
```java
public boolean validateToken(String token) {
    try {
        JWT.require(algorithm)
           .withIssuer(issuer)
           .build()
           .verify(token);  // throws exception if invalid/expired
        return true;
    } catch (JWTVerificationException e) {
        return false;
    }
}
```

---

## 4.6 SignupRedisService.java — Redis Operations

```java
@Service
@RequiredArgsConstructor
public class SignupRedisService {

    private final StringRedisTemplate redisTemplate;  // Spring's Redis client
    private final ObjectMapper objectMapper;           // JSON serializer

    // Store OTP with 5-minute expiry
    public void storeOtp(String email, String otp) {
        String key = "OTP:" + email;   // Redis key
        redisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
        // After 5 minutes, Redis auto-deletes this key
    }

    // Store entire SignupRequest as JSON (so verifyOtp can retrieve it)
    public void storeSignupRequest(SignupRequest request) {
        String key = "SIGNUP:" + request.getEmail();
        String json = objectMapper.writeValueAsString(request);  // convert object → JSON string
        redisTemplate.opsForValue().set(key, json, 10, TimeUnit.MINUTES);
    }

    // Retrieve and deserialize SignupRequest from Redis
    public SignupRequest getSignupRequest(String email) {
        String key = "SIGNUP:" + email;
        String json = redisTemplate.opsForValue().get(key);  // get value
        return objectMapper.readValue(json, SignupRequest.class);  // JSON → object
    }

    // Validate OTP
    public boolean validateOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP:" + email);
        if (storedOtp == null) return false;      // expired or doesn't exist
        boolean isValid = storedOtp.equals(otp);
        if (isValid) deleteOtp(email);            // delete after successful use
        return isValid;
    }
}
```

---

## 4.7 UserClient.java — Feign Client

**What is OpenFeign?**
Feign lets you call another service's REST API using a Java interface — no HttpClient, no RestTemplate boilerplate. Just declare the interface and Spring creates the implementation automatically.

```java
@FeignClient(name = "user-service", path = "/api/users")
// name = "user-service" → Feign asks Eureka: "Where is user-service?"
// path = "/api/users"   → Base path for all methods

public interface UserClient {

    @PostMapping("/auth/register")
    ApiResponse<UserServiceUserDto> registerUser(@RequestBody CreateUserCredentialRequest request);
    // This calls: POST http://user-service/api/users/auth/register

    @GetMapping("/auth/email/{email}/exists")
    ApiResponse<Boolean> checkEmailExists(@PathVariable String email);
    // This calls: GET http://user-service/api/users/auth/email/koushik@gmail.com/exists

    @PostMapping("/profile")
    ApiResponse<Object> createProfile(
            @RequestBody UserProfileDto profileData,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String email);
    // Sends custom headers with the request
}
```

**Feign + Eureka = Magic:**
Feign doesn't need to know the IP or port of `user-service`. It asks Eureka for the address automatically. If `user-service` has 3 instances running, Feign load-balances between them.

---

## 4.8 SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // CSRF protection disabled because we use JWT (stateless API, no cookies)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // No sessions! Every request must carry a JWT token.

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/verify-otp",
                    "/api/auth/forgot-password",
                    "/swagger-ui/**"
                ).permitAll()          // These URLs don't need authentication
                .anyRequest().authenticated()  // Everything else requires a token
            );

        return http.build();
    }
}
```

**Why disable CSRF?**
CSRF (Cross-Site Request Forgery) protection is needed for cookie-based sessions. Since this is a stateless JWT API, there are no cookies to steal, so CSRF is not needed.

---

## 4.9 RabbitMQConfig.java

```java
@Configuration
public class RabbitMQConfig {

    // Constants (reused everywhere to avoid typos)
    public static final String EXCHANGE = "notification.exchange";
    public static final String OTP_QUEUE = "otp.queue";
    public static final String OTP_ROUTING_KEY = "otp.send";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE);
        // TopicExchange supports wildcard routing keys like "otp.*"
    }

    @Bean
    public Queue otpQueue() {
        return new Queue(OTP_QUEUE, true);
        // true = durable: survives RabbitMQ restart
    }

    @Bean
    public Binding otpBinding() {
        return BindingBuilder
            .bind(otpQueue())
            .to(notificationExchange())
            .with(OTP_ROUTING_KEY);
        // Route: message with key "otp.send" → goes to "otp.queue"
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
        // Converts Java objects to JSON when publishing to RabbitMQ
        // Converts JSON back to Java objects when consuming from RabbitMQ
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
        // This is what you inject to publish messages
    }
}
```

---

## 4.10 EventProducer.java

```java
@Component
@RequiredArgsConstructor
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:notification.exchange}")
    private String exchange;  // reads from application.yml, default value after ":"

    public void publishOtpEvent(OtpEvent event) {
        rabbitTemplate.convertAndSend(exchange, "otp.send", event);
        // exchange = "notification.exchange"
        // routing key = "otp.send"
        // message = event (serialized to JSON by Jackson2JsonMessageConverter)
    }
}
```

---

# PART 5: USER SERVICE — ENTITIES AND MAPPING

---

## 5.1 The Four Tables in user-service

```
user_credentials          user_profiles
┌────────────────┐        ┌───────────────────────┐
│ id (PK)        │        │ id (PK) = same as      │
│ email (UNIQUE) │        │   user_credentials.id  │
│ password       │        │ email                  │
│ name           │        │ name                   │
│ phone          │        │ phone                  │
│ role           │        │ avatarUrl              │
│ emailVerified  │        │ createdAt              │
│ enabled        │        └───────────────────────┘
│ createdAt      │
└────────────────┘

addresses                 refresh_tokens
┌────────────────┐        ┌───────────────┐
│ id (PK)        │        │ id (PK)       │
│ user_id (FK)   │        │ token (UNIQUE)│
│ street         │        │ userId        │
│ city           │        │ email         │
│ state          │        │ expiresAt     │
│ zipCode        │        │ revoked       │
│ country        │        └───────────────┘
│ label          │
│ isDefault      │
└────────────────┘
```

**Why separate `user_credentials` and `user_profiles`?**

Security separation. `user_credentials` stores the password hash. `user_profiles` stores display information. If you expose the profile API, you never accidentally leak the password.

---

## 5.2 UserCredential.java Entity

```java
@Entity
@Table(name = "user_credentials")
@Data                   // getters + setters
@NoArgsConstructor      // empty constructor (required by JPA)
@AllArgsConstructor     // all-args constructor
@Builder                // enables builder pattern: UserCredential.builder().email("x").build()
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment in MySQL
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;   // ALWAYS stored as BCrypt hash, NEVER plain text

    @Column(nullable = false, length = 20)
    private String role;        // "ROLE_USER" or "ROLE_ADMIN"

    @PrePersist                // runs automatically BEFORE first save to database
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) role = "ROLE_USER";
        enabled = true;
    }

    @PreUpdate                 // runs automatically BEFORE every update
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 5.3 UserProfile.java — One-to-Many Relationships

```java
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id;   // Same ID as UserCredential — shared primary key!

    // ONE user has MANY addresses
    @OneToMany(
        mappedBy = "user",          // "user" = field name in Address entity
        cascade = CascadeType.ALL,  // delete user → delete all their addresses
        orphanRemoval = true        // remove address from list → delete from DB
    )
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishlistItem> wishlistItems = new ArrayList<>();
}
```

**What is `mappedBy`?**
It tells JPA: "The `user` field in the `Address` class holds the foreign key (`user_id`), not here."

---

## 5.4 Address.java — ManyToOne Relationship

```java
@Entity
@Table(name = "addresses")
public class Address {

    @ManyToOne(fetch = FetchType.LAZY)   // MANY addresses belong to ONE user
    @JoinColumn(name = "user_id", nullable = false)
    // Creates column "user_id" in addresses table = foreign key
    private UserProfile user;
}
```

**`FetchType.LAZY`** = Don't load the User when you load an Address. Load it only when you explicitly access `address.getUser()`. This prevents loading unnecessary data.

**`FetchType.EAGER`** = Load the User immediately when loading an Address. (Usually bad for performance.)

---

## 5.5 UserMapper.java — MapStruct

**What is MapStruct?**
MapStruct is a code generator that creates the conversion code between Entity and DTO automatically at compile time.

```java
@Mapper(componentModel = "spring")  // Spring manages this as a @Component
public interface UserMapper {

    // MapStruct generates the implementation automatically:
    UserProfileDto toUserProfileDto(UserProfile userProfile);
    // Generated code: new UserProfileDto(profile.getId(), profile.getEmail(), ...)

    UserProfile toUserProfile(UserProfileDto userProfileDto);
    // Generated code: new UserProfile(dto.getId(), dto.getEmail(), ...)

    @Mapping(target = "user", ignore = true)   // Skip "user" field when mapping
    @Mapping(target = "id", ignore = true)     // Skip "id" field (auto-generated)
    Address toAddress(AddressDto addressDto);

    List<UserProfileDto> toUserProfileDtoList(List<UserProfile> userProfiles);
    // MapStruct generates: userProfiles.stream().map(this::toUserProfileDto).collect(...)
}
```

**Why use MapStruct instead of writing conversion manually?**
Without MapStruct you'd write:
```java
// Boring, error-prone manual mapping:
UserProfileDto dto = new UserProfileDto();
dto.setId(profile.getId());
dto.setEmail(profile.getEmail());
dto.setName(profile.getName());
// ... 10 more fields
```

MapStruct generates this code automatically. Less code = fewer bugs.

---

## 5.6 UserService.java — CRUD with Repository

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    public ApiResponse<UserProfileDto> getProfile(Long userId) {

        // Find by ID, throw exception if not found
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile", "id", userId));
        //  ↑ Optional.orElseThrow() — if empty, throw exception

        // Convert entity → DTO (never send raw entity to client!)
        return ApiResponse.success("Profile retrieved", userMapper.toUserProfileDto(profile));
    }

    @Transactional   // All DB operations succeed together or all fail together
    public ApiResponse<AddressDto> addAddress(Long userId, AddressDto addressDto) {

        // 1. Find the user
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile", "id", userId));

        // 2. Convert DTO to entity
        Address address = userMapper.toAddress(addressDto);

        // 3. Set the relationship (address knows which user it belongs to)
        address.setUser(profile);

        // 4. Save to database
        address = addressRepository.save(address);

        // 5. Return DTO (not entity!)
        return ApiResponse.success("Address added", userMapper.toAddressDto(address));
    }
}
```

**Why `@Transactional`?**
Imagine you're updating a user's address AND setting it as default (clearing other default addresses). These are two database operations. `@Transactional` ensures:
- If BOTH succeed → changes are committed
- If EITHER fails → ALL changes are rolled back (no partial updates)

---

# PART 6: PRODUCT SERVICE — MONGODB

---

## 6.1 Product.java — MongoDB Document

```java
@Document(collection = "products")  // MongoDB equivalent of @Entity + @Table
@Data
@Builder
public class Product {

    @Id
    private String id;   // MongoDB auto-generates a string ID like "64f3b2c1a2e4d5f6"

    @Indexed(unique = true)  // MongoDB index for fast lookups + uniqueness
    private String productCode;

    @Indexed   // Index for fast searches (no uniqueness constraint)
    private String name;

    private BigDecimal price;
    private int stock;
    private String category;

    @Builder.Default
    private List<String> images = new ArrayList<>();  // MongoDB can store arrays natively

    private Map<String, String> specifications;       // MongoDB can store nested objects natively

    @CreatedDate    // Spring Data automatically sets this on save
    private LocalDateTime createdAt;

    @LastModifiedDate  // Spring Data automatically sets this on update
    private LocalDateTime updatedAt;
}
```

---

## 6.2 ProductRepository.java — MongoDB Queries

```java
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    // MongoRepository<EntityType, IdType>
    // Provides: save(), findById(), findAll(), delete(), etc. automatically

    // Spring Data generates the query from the method name:
    Optional<Product> findByProductCode(String productCode);
    // Generated query: db.products.findOne({productCode: "..."})

    Page<Product> findByActiveTrue(Pageable pageable);
    // Generated query: db.products.find({active: true}).skip(0).limit(10)

    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);
    // Generated query: db.products.find({category: "Electronics", active: true})

    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'description': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'brand': { '$regex': ?0, '$options': 'i' } } ] }")
    Page<Product> searchProducts(String keyword, Pageable pageable);
    // Custom MongoDB query using regex for case-insensitive search
}
```

---

## 6.3 ProductService.java — Pagination

```java
public ApiResponse<ProductPageResponse> getProducts(int page, int size, String sortBy, String sortDir) {

    // Build sort direction
    Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

    // Pageable = page number + page size + sort
    Pageable pageable = PageRequest.of(page, size, sort);

    // Query MongoDB with pagination
    Page<Product> productPage = productRepository.findByActiveTrue(pageable);

    // Build response with pagination metadata
    return ApiResponse.success("Products retrieved", ProductPageResponse.builder()
            .content(productMapper.toProductDtoList(productPage.getContent()))
            .page(productPage.getNumber())
            .size(productPage.getSize())
            .totalElements(productPage.getTotalElements())
            .totalPages(productPage.getTotalPages())
            .last(productPage.isLast())
            .build());
}
```

**Soft Delete** (notice `deleteProduct`):
```java
public ApiResponse<Void> deleteProduct(String id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    product.setActive(false);   // Don't actually delete! Just mark as inactive.
    productRepository.save(product);
    return ApiResponse.success("Product deleted successfully");
}
```
This is called a **soft delete**. The record remains in the database but is hidden from all queries (because queries filter `active: true`). This is good for auditing — you can recover deleted products.

---

# PART 7: ORDER SERVICE — CART + ORDERS

---

## 7.1 CartRedisService.java — Shopping Cart

```java
@Service
public class CartRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CART_PREFIX = "CART:";

    public void addToCart(Long userId, CartItemDto item) {
        String key = "CART:" + userId;      // e.g., "CART:1"
        List<CartItemDto> cartItems = getCartItems(userId);  // get existing cart

        // If product already in cart, increase quantity
        boolean found = false;
        for (CartItemDto cartItem : cartItems) {
            if (cartItem.getProductId().equals(item.getProductId())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) cartItems.add(item);  // New product, add to cart

        saveCart(key, cartItems);  // serialize and save back to Redis
    }

    private void saveCart(String key, List<CartItemDto> items) {
        String json = objectMapper.writeValueAsString(items);
        redisTemplate.opsForValue().set(key, json, 24, TimeUnit.HOURS);
        // Cart expires after 24 hours of inactivity
    }
}
```

---

## 7.2 Order.java Entity — OneToMany

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;    // Which user placed this order

    private String orderNumber;   // e.g., "ORD-1703425891234"

    @Enumerated(EnumType.STRING)  // Store as "PENDING" not 0, 1, 2
    private OrderStatus orderStatus;   // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;  // PENDING, COMPLETED, FAILED, REFUNDED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        orderNumber = "ORD-" + System.currentTimeMillis();  // unique order number
    }
}
```

---

## 7.3 OrderService.java — createOrder with Feign

```java
@Transactional
public ApiResponse<OrderDto> createOrder(Long userId, CreateOrderRequest request) {

    BigDecimal totalAmount = BigDecimal.ZERO;
    List<OrderItem> orderItems = new ArrayList<>();

    // For each item in the order request:
    for (OrderItemDto itemDto : request.getItems()) {

        // Validate the product exists via Feign call to product-service
        ApiResponse<ProductDto> productResponse = productClient.getProduct(itemDto.getProductId());
        ProductDto product = productResponse.getData();

        // Calculate subtotal
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
        totalAmount = totalAmount.add(subtotal);

        // Build OrderItem entity
        OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(itemDto.getQuantity())
                .price(product.getPrice())
                .subtotal(subtotal)
                .build();
        orderItems.add(orderItem);
    }

    // Build Order entity
    Order order = Order.builder()
            .userId(userId)
            .totalAmount(totalAmount)
            .paymentStatus(PaymentStatus.PENDING)
            .orderStatus(OrderStatus.PENDING)
            .orderItems(orderItems)
            .build();

    // Link each item back to the order (bidirectional relationship)
    orderItems.forEach(item -> item.setOrder(order));

    // Save order (cascade saves all order items too)
    Order savedOrder = orderRepository.save(order);

    return ApiResponse.success("Order created", orderMapper.toOrderDto(savedOrder));
}
```

---

# PART 8: NOTIFICATION SERVICE

---

## 8.1 NotificationConsumer.java

```java
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "otp.queue")
    // Spring automatically: picks messages from "otp.queue"
    //                       deserializes JSON → OtpEvent object
    //                       calls this method
    public void handleOtpEvent(OtpEvent event) {
        emailService.sendOtpEmail(event.getEmail(), event.getFirstName(), event.getOtp());
    }

    @RabbitListener(queues = "welcome.queue")
    public void handleWelcomeEvent(WelcomeEvent event) {
        emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName(), event.getLastName());
    }

    @RabbitListener(queues = "forgot.password.queue")
    public void handleForgotPasswordEvent(ForgotPasswordEvent event) {
        emailService.sendPasswordResetEmail(event.getEmail(), event.getFirstName(), event.getOtp());
    }
}
```

---

## 8.2 EmailService.java — Thymeleaf + JavaMail

```java
@Service
public class EmailService {

    private final JavaMailSender mailSender;      // Spring's email sender
    private final TemplateEngine templateEngine;  // Thymeleaf template engine

    public void sendOtpEmail(String to, String firstName, String otp) {

        // Set template variables
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("otp", otp);
        context.setVariable("expiryMinutes", 5);

        // Process Thymeleaf template → HTML string
        String htmlContent = templateEngine.process("otp-email", context);
        // Reads: src/main/resources/templates/otp-email.html
        // Replaces: th:text="${firstName}" → "Koushik"
        //           th:text="${otp}" → "847291"

        sendEmail(to, "Your OTP", htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);  // true = HTML content
        mailSender.send(message);
    }
}
```

---

# PART 9: API GATEWAY — JWT FILTER

---

## 9.1 JwtAuthenticationFilter.java

```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    // GlobalFilter = runs for EVERY request through the gateway
    // Ordered = controls execution order (getOrder() returns 2)

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 1. Skip filter for public APIs (no token needed)
        boolean isPublicApi = List.of("/api/auth/login", "/api/auth/signup", "/swagger-ui", "/v3/api-docs")
                .stream().anyMatch(path::startsWith);
        if (isPublicApi) return chain.filter(exchange);  // pass through

        // 2. Get Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization Header Missing");
        }

        // 3. Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // 4. Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid JWT Token");
        }

        // 5. Extract user info from token
        String email = jwtUtil.retrieveEmailFromToken(token);
        List<String> roles = jwtUtil.retrieveRolesFromToken(token);

        // 6. Check role-based access (RBAC)
        String routeKey = exchange.getRequest().getMethod().name() + ":" + determineRoute(path);
        List<String> allowedRoles = authorizationConfig.routeRoles().get(routeKey);
        if (allowedRoles != null) {
            boolean authorized = roles.stream().anyMatch(allowedRoles::contains);
            if (!authorized) throw new ForbiddenException("Access Denied");
        }

        // 7. Forward to downstream service
        return chain.filter(exchange);
    }
}
```

**`ServerWebExchange`** is the reactive equivalent of `HttpServletRequest` + `HttpServletResponse`. It's used because Spring Cloud Gateway is built on WebFlux (reactive/non-blocking).

**`Mono<Void>`** is a reactive type — it represents an async operation that completes without returning a value. `chain.filter(exchange)` passes the request to the next filter or the downstream service.

---

# PART 10: EXCEPTION HANDLING

---

## GlobalExceptionHandler.java (every service has one)

```java
@RestControllerAdvice   // Applies to all @RestController classes globally
public class GlobalExceptionHandler {

    // When BusinessException is thrown anywhere, this method handles it
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())  // e.g., "Email already registered"
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())  // e.g., "User not found with id: 5"
                .build();
    }

    // Handles @Valid validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.<Void>builder().success(false).message(message).build();
    }
}
```

**`ResourceNotFoundException`:**
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " not found with " + field + ": " + value);
        // e.g., "User not found with id: 5"
    }
}
```

---

# PART 11: HOW TO BUILD YOUR OWN PROJECT FROM SCRATCH

---

## Step-by-Step: Build a New Microservice

### Step 1: Create the Spring Boot project
Go to `https://start.spring.io/` and select:
- **Spring Web** — for REST APIs
- **Spring Data JPA** — for MySQL
- **MySQL Driver**
- **Spring Cloud Netflix Eureka Client** — to register with Eureka
- **Lombok** — to reduce boilerplate
- **Spring Validation** — for @Valid annotations

### Step 2: Create the package structure
```
com.ecommerce.yourservice/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
├── config/
└── exception/
```

### Step 3: Create the Entity
```java
@Entity
@Table(name = "your_table")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class YourEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
```

### Step 4: Create the Repository
```java
@Repository
public interface YourRepository extends JpaRepository<YourEntity, Long> {
    Optional<YourEntity> findByName(String name);
    // Spring Data generates the SQL automatically
}
```

### Step 5: Create the DTO
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class YourDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
}
```

### Step 6: Create the Mapper
```java
@Mapper(componentModel = "spring")
public interface YourMapper {
    YourDto toDto(YourEntity entity);
    YourEntity toEntity(YourDto dto);
    List<YourDto> toDtoList(List<YourEntity> entities);
}
```

### Step 7: Create the Service
```java
@Service @RequiredArgsConstructor @Slf4j
public class YourService {
    private final YourRepository repository;
    private final YourMapper mapper;

    public ApiResponse<YourDto> create(YourDto dto) {
        YourEntity entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return ApiResponse.success("Created successfully", mapper.toDto(entity));
    }

    public ApiResponse<YourDto> getById(Long id) {
        YourEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity", "id", id));
        return ApiResponse.success("Retrieved", mapper.toDto(entity));
    }
}
```

### Step 8: Create the Controller
```java
@RestController
@RequestMapping("/api/yours")
@RequiredArgsConstructor
public class YourController {
    private final YourService service;

    @PostMapping
    public ResponseEntity<ApiResponse<YourDto>> create(@Valid @RequestBody YourDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<YourDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
```

### Step 9: Configure application.yml
```yaml
server:
  port: 8090

spring:
  application:
    name: your-service
  datasource:
    url: jdbc:mysql://localhost:3306/your_db?createDatabaseIfNotExist=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update    # auto-create/update tables

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Step 10: Create Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

# PART 12: LOMBOK ANNOTATIONS CHEAT SHEET

| Annotation | What it generates |
|------------|-------------------|
| `@Data` | getters + setters + toString + equals + hashCode |
| `@Getter` | only getters |
| `@Setter` | only setters |
| `@NoArgsConstructor` | empty constructor |
| `@AllArgsConstructor` | constructor with all fields |
| `@RequiredArgsConstructor` | constructor for `final` fields only |
| `@Builder` | builder pattern: `User.builder().name("x").build()` |
| `@Slf4j` | creates `log` variable for logging |

---

# PART 13: SPRING ANNOTATIONS CHEAT SHEET

| Annotation | Where used | What it does |
|------------|------------|--------------|
| `@SpringBootApplication` | Main class | Starts Spring Boot |
| `@RestController` | Controller | Returns JSON from all methods |
| `@RequestMapping` | Controller | Base URL prefix |
| `@GetMapping` | Method | Maps to HTTP GET |
| `@PostMapping` | Method | Maps to HTTP POST |
| `@PutMapping` | Method | Maps to HTTP PUT |
| `@DeleteMapping` | Method | Maps to HTTP DELETE |
| `@PathVariable` | Parameter | Reads from URL path: `/users/{id}` |
| `@RequestParam` | Parameter | Reads from query string: `?page=0` |
| `@RequestBody` | Parameter | Reads JSON body |
| `@RequestHeader` | Parameter | Reads HTTP header |
| `@Service` | Class | Business logic layer |
| `@Repository` | Class | Database layer |
| `@Component` | Class | Generic Spring bean |
| `@Configuration` | Class | Config class with `@Bean` methods |
| `@Bean` | Method | Defines a Spring-managed object |
| `@Autowired` | Field | Injects dependency (prefer constructor injection) |
| `@Value` | Field | Reads value from application.yml |
| `@Transactional` | Method | Wraps in DB transaction |
| `@Entity` | Class | Maps to DB table |
| `@Table` | Class | Specifies table name |
| `@Id` | Field | Primary key |
| `@GeneratedValue` | Field | Auto-increment ID |
| `@Column` | Field | Column constraints |
| `@OneToMany` | Field | One-to-many relationship |
| `@ManyToOne` | Field | Many-to-one relationship |
| `@JoinColumn` | Field | Specifies FK column name |
| `@Document` | Class | MongoDB collection |
| `@EnableEurekaServer` | Main class | Makes it a Eureka server |
| `@EnableFeignClients` | Main class | Enables Feign HTTP clients |
| `@FeignClient` | Interface | Declares a Feign HTTP client |
| `@RabbitListener` | Method | Consumes RabbitMQ messages |

---

# SUMMARY: THE BIG PICTURE

```
SIGNUP FLOW (complete journey):

1. POST /api/auth/signup → API Gateway
2. Gateway validates: no JWT needed (public API), forwards to auth-service
3. auth-service: checks email via Feign → user-service
4. auth-service: generates OTP, stores in Redis (5 min TTL)
5. auth-service: publishes OtpEvent to RabbitMQ "notification.exchange"
6. notification-service: consumes from "otp.queue", sends email via Gmail SMTP
7. Returns: {"success": true, "message": "OTP sent"}

OTP VERIFY FLOW:

1. POST /api/auth/verify-otp → API Gateway → auth-service
2. auth-service: validates OTP from Redis
3. auth-service: creates user via Feign → user-service (saves to MySQL)
4. auth-service: generates JWT access token + refresh token
5. auth-service: saves refresh token via Feign → user-service
6. auth-service: publishes WelcomeEvent → notification-service sends welcome email
7. Returns: {"success": true, "data": {"accessToken": "...", "refreshToken": "..."}}

AUTHENTICATED REQUEST FLOW:

1. GET /api/products?page=0&size=10
   Headers: Authorization: Bearer eyJhbGci...
2. API Gateway: JwtAuthenticationFilter
   → extracts token
   → validates signature + expiry
   → checks RBAC rules
   → forwards to product-service
3. product-service: queries MongoDB with pagination
4. Returns: paginated list of products
```

This is the complete architecture of your project. Every class, every annotation, every tool — all working together to build a production-grade ecommerce backend.
