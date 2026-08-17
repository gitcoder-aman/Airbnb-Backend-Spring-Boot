# 🏨 AtithiStay — Interview Preparation Guide
> **Spring Boot Backend | Airbnb Clone | Full Project Walkthrough**

---

## 📌 1. Project Overview (Elevator Pitch)

**What is this project?**

AtithiStay is a **production-ready Airbnb-clone backend** that powers a hotel booking platform. It supports two user roles — **Guests** (travellers) and **Hotel Managers** (admins). The system handles the full booking lifecycle: hotel search → room reservation → payment → check-in → review.

**Key highlights to mention in an interview:**
- JWT-based stateless authentication with Access + Refresh tokens
- Stripe Checkout for real payment sessions with webhook handling
- Dynamic pricing engine that recalculates prices every 10 minutes using 4 composable strategies (Decorator Pattern)
- Google Gemini AI integration via Spring AI
- Flyway for versioned database migrations
- Swagger UI for interactive API documentation
- Scheduled jobs for booking expiration and price updates

---

## 🛠️ 2. Tech Stack (Know Every Library)

| Layer | Technology | Why Used |
|---|---|---|
| Framework | **Spring Boot 4.0.5** | Auto-configuration, embedded server |
| Language | **Java 21** | LTS version, virtual threads support |
| Database | **PostgreSQL** | Relational DB, ACID compliance |
| ORM | **Spring Data JPA / Hibernate** | Object-relational mapping |
| Migrations | **Flyway** | Versioned, repeatable DB migrations |
| Security | **Spring Security + JWT (JJWT 0.11.5)** | Stateless auth |
| Payments | **Stripe Java SDK 28.2.0** | Payment sessions, refunds, webhooks |
| AI | **Spring AI + Google Gemini 2.5 Flash** | AI chat assistant |
| Mapping | **ModelMapper 3.2.2** | Entity ↔ DTO conversion |
| API Docs | **SpringDoc OpenAPI (Swagger UI)** | Auto-generated API documentation |
| Build | **Maven** | Dependency management |
| Boilerplate | **Lombok** | `@Getter`, `@Setter`, `@Builder`, etc. |

---

## 🏗️ 3. Architecture (How Layers Connect)

```
Client (Android App / Postman)
         │
         ▼
  ┌─────────────────────┐
  │   REST Controllers   │  ← Receives HTTP requests
  └────────┬────────────┘
           │
  ┌────────▼────────────┐
  │   Service Layer      │  ← Business logic lives here
  │  (Interface + Impl)  │
  └────────┬────────────┘
           │
  ┌────────▼────────────┐
  │  Repository Layer    │  ← Spring Data JPA
  │  (JPA Repositories)  │
  └────────┬────────────┘
           │
  ┌────────▼────────────┐
  │    PostgreSQL DB     │  ← Managed by Flyway migrations
  └─────────────────────┘
```

**Cross-cutting concerns:**
- `JwtAuthFilter` → validates JWT on every request before controllers
- `GlobalExceptionHandler` (in `advice/`) → handles all exceptions centrally
- `@Scheduled` jobs → pricing updates + booking expiration (run in background)

---

## 📦 4. Package Structure Explained

```
com.tech.project.AirbnbBackend/
├── controllers/
│   ├── admin/          → Hotel CRUD, Room, Inventory (HOTEL_MANAGER only)
│   ├── user/           → Auth, Search, Booking, Reviews (public/authenticated)
│   ├── ai/             → AI chat endpoint
│   └── WebhookController.java  → Stripe webhook handler
├── entities/           → JPA entity classes
├── services/
│   ├── *.java          → Service interfaces
│   └── impl/           → Concrete implementations
├── strategy/           → Dynamic pricing strategies (Decorator Pattern)
├── security/           → JWT filter, config, AuthService
├── dto/                → Data Transfer Objects (request/response)
├── repositories/       → Spring Data JPA interfaces
├── advice/             → @ControllerAdvice global exception handler
├── exception/          → Custom exception classes
├── config/             → Bean configurations (ModelMapper, Stripe)
└── utils/              → Helper: getCurrentUser()
```

---

## 👤 5. Authentication & Security (Deep Dive)

### Flow

```
POST /api/v1/auth/signup  →  Creates user with ROLE_GUEST, BCrypt password
POST /api/v1/auth/login   →  Returns [accessToken, refreshToken]
POST /api/v1/auth/refresh →  Reads refreshToken from cookie → new accessToken
```

### JwtService — Token Generation

```java
// Access Token — contains userId, email, roles
Jwts.builder()
    .setSubject(user.getId().toString())
    .claim("email", user.getEmail())
    .claim("roles", user.getRoles().toString())
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + 1000L*60*60*24*30*6))
    .signWith(getSecretKey())
    .compact();

// Refresh Token — only userId
Jwts.builder()
    .setSubject(user.getId().toString())
    .setExpiration(...)
    .signWith(getSecretKey())
    .compact();
```

### JwtAuthFilter — Request Interception

- Runs **before** `UsernamePasswordAuthenticationFilter`
- Extracts `Authorization: Bearer <token>` header
- Calls `JwtService.getUserIdFromToken()` to get userId
- Loads user from DB, sets `SecurityContextHolder`

### WebSecurityConfig — Route Rules

```
/api/v1/admin/**          → ROLE_HOTEL_MANAGER only
/api/v1/auth/**           → Anonymous (unauthenticated only)
/api/v1/bookings/**       → Any authenticated user
/api/v1/users/**          → Any authenticated user
POST /api/v1/rooms/*/reviews → ROLE_GUEST only
GET  /api/v1/reviews/**   → Public (anyone)
Everything else           → Public
```

- **Session policy**: `STATELESS` — no server-side sessions
- **Password hashing**: `BCryptPasswordEncoder`
- **Exception handling**: Custom `AccessDeniedHandler` routes to `HandlerExceptionResolver`

---

## 🗄️ 6. Database Entities & Relationships

### Entity Relationship Summary

```
User ────(1:N)──── Hotel          (owner field)
Hotel ───(1:N)──── Room
Hotel ───(1:N)──── Inventory
Room ────(1:N)──── Inventory
User ────(1:N)──── Booking        (guest/user field)
Hotel ───(1:N)──── Booking
Room ────(1:N)──── Booking
Booking ─(M:N)──── Guest         (booking_guests join table)
Hotel ───(1:N)──── HotelMinPrice  (daily min price cache)
Room ────(1:N)──── Review
```

### Key Entity: `Inventory` (Heart of the System)

```java
@Table(uniqueConstraints = @UniqueConstraint(
    columnNames = {"hotel_id","room_id","date"}  // Composite unique key
))
public class Inventory {
    private Hotel hotel;
    private Room room;
    private LocalDate date;       // One record per room per day
    private Integer bookedCount;  // Rooms fully confirmed booked
    private Integer reservedCount;// Rooms in PENDING/RESERVED state
    private Integer totalCount;   // Total rooms available
    private BigDecimal surgeFactor; // Applied by SurgeStrategy
    private BigDecimal price;     // Dynamically updated every 10 min
    private String city;
    private Boolean closed;       // Admin can close a date manually
}
```

### Key Entity: `Booking`

```java
public class Booking {
    private Hotel hotel;
    private Room room;
    private User user;
    private Integer roomCount;
    private LocalDate checkInDate, checkOutDate;
    private BookingStatus bookingStatus;   // See lifecycle below
    private Set<Guest> guests;             // ManyToMany
    private BigDecimal totalAmount;        // subTotal + tax
    private BigDecimal taxAmount;          // 10%
    private BigDecimal subTotalAmount;
    private String paymentSessionId;       // Stripe session ID
}
```

### BookingStatus Enum (Full Lifecycle)

```
RESERVED → GUEST_ADDED → PAYMENT_PENDING → CONFIRMED → CHECKED_IN → COMPLETED
                                                       → CANCELLED
         → EXPIRED (if not paid within 10 minutes)
```

---

## 📅 7. Booking Lifecycle (Step by Step)

### Step 1: `POST /api/v1/bookings/init` — Initialise Booking

1. Fetch `Hotel` and `Room` from DB
2. Call `inventoryRepository.findAndLockAvailableInventory()` — **pessimistic lock** to prevent race conditions
3. Check if available inventory count >= days requested
4. Call `inventoryRepository.initBooking()` — increments `reservedCount`
5. Calculate `subTotal` (sum of per-day prices × rooms), `tax` (10%), `total`
6. Create `Booking` with status `RESERVED`
7. Return `BookingDto`

### Step 2: `POST /api/v1/bookings/{id}/addGuests`

1. Validate booking belongs to current user
2. Check booking not expired (`createdAt + 10 min > now`)
3. Validate status is `RESERVED`
4. Save each `Guest` entity, add to booking's guest set
5. Update status to `GUEST_ADDED`

### Step 3: `POST /api/v1/bookings/{id}/payment`

1. Validate booking belongs to current user & not expired
2. Call `CheckoutService.getCheckOutSession()` → creates **Stripe Checkout Session**
3. Update status to `PAYMENT_PENDING`
4. Return the Stripe session URL to client

### Step 4: Stripe Webhook — `POST /webhook`

1. Stripe calls our webhook after payment succeeds
2. Verify signature using `STRIPE_WEBHOOK_SECRET`
3. Parse `checkout.session.completed` event
4. Find booking by `paymentSessionId`
5. Set status → `CONFIRMED`
6. Lock inventory and call `inventoryRepository.confirmBooking()` — moves `reservedCount` → `bookedCount`

### Step 5: `POST /api/v1/bookings/{id}/cancel`

1. Only `CONFIRMED` bookings can be cancelled
2. Set status → `CANCELLED`
3. Release inventory (`bookedCount` decremented)
4. Trigger **Stripe Refund** via `Refund.create()`

### Step 6: `POST /api/v1/bookings/{id}/check-in` (HOTEL_MANAGER)

1. Validate current date is between check-in and check-out
2. Validate booking status is `CONFIRMED`
3. Set status → `CHECKED_IN`

### Scheduled: Auto-Complete Bookings

```java
@Scheduled(cron = "0 * * * * *") // every minute
public void updateCompletedBookings() {
    // finds CHECKED_IN bookings where checkOutDate < today
    // sets status → COMPLETED
}
```

### Scheduled: Auto-Expire Bookings

```java
@Scheduled(cron = "0 */10 * * * *") // every 10 minutes
public void expireBookings() {
    // finds RESERVED/GUEST_ADDED/PAYMENT_PENDING bookings older than 3 min
    // sets status → EXPIRED
    // releases reserved inventory
}
```

---

## 💹 8. Dynamic Pricing Engine (Design Pattern: Decorator)

### The Strategy Interface

```java
public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
```

### PriceService — Chains All Strategies

```java
public BigDecimal calculateDynamicPricing(Inventory inventory) {
    PricingStrategy pricingStrategy = new BasePricingStrategy();
    pricingStrategy = new SurgePriceStrategy(pricingStrategy);
    pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
    pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
    pricingStrategy = new HolidayPriceStrategy(pricingStrategy);
    return pricingStrategy.calculatePrice(inventory);
}
```

**Execution order:** Base → Surge → Occupancy → Urgency → Holiday

### Each Strategy Explained

| Strategy | Logic | Multiplier |
|---|---|---|
| `BasePricingStrategy` | Returns `room.basePrice` as starting point | 1.0x (floor) |
| `SurgePriceStrategy` | Multiplies by `inventory.surgeFactor` | Variable |
| `OccupancyPricingStrategy` | If `bookedCount/totalCount > 80%` → increase | 1.2x |
| `UrgencyPricingStrategy` | If date is within next 7 days → increase | 1.15x |
| `HolidayPriceStrategy` | If date falls on a public holiday → increase | Custom |

### PricingUpdateService — Scheduled Every 10 Minutes

```java
@Scheduled(cron = "0 */10 * * * *")
public void updatePrice() {
    // Processes hotels in batches of 100 (pagination)
    // For each hotel:
    //   1. Fetch all inventory for next 365 days
    //   2. Run each through PriceService → update price
    //   3. Find min price per date → save to HotelMinPrice table
    //   4. Update hotel.startingPrice (displayed on browse/search)
}
```

---

## 💳 9. Stripe Payment Integration

### Creating a Checkout Session

```java
// CheckOutServiceImpl
SessionCreateParams params = SessionCreateParams.builder()
    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
    .setMode(SessionCreateParams.Mode.PAYMENT)
    .setSuccessUrl(successUrl)
    .setCancelUrl(cancelUrl)
    .addLineItem(...)  // booking total amount
    .setClientReferenceId(booking.getId().toString())  // our booking ID
    .build();

Session session = Session.create(params);
booking.setPaymentSessionId(session.getId());
return session.getUrl(); // redirect URL sent to client
```

### Webhook Verification

```java
// WebhookController
Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
// Stripe signs the payload with STRIPE_WEBHOOK_SECRET
// constructEvent() throws SignatureVerificationException if invalid
bookingService.capturePayment(event);
```

### Refund on Cancellation

```java
Session session = Session.retrieve(booking.getPaymentSessionId());
RefundCreateParams params = RefundCreateParams.builder()
    .setPaymentIntent(session.getPaymentIntent())
    .build();
Refund.create(params);
```

---

## 🔍 10. Hotel Search & Browse

### Search — `POST /api/v1/hotels/search`

Request body: `city`, `checkInDate`, `checkOutDate`, `numberOfRooms`

Logic in `InventoryServiceImpl`:
1. Query `inventory` table for city, date range, available rooms
2. Group by hotel, filter hotels with sufficient availability on ALL dates
3. Return paginated results with `startingPrice`

### Room Inventory Auto-Generation

When a new room is created, the system auto-generates **365 inventory records** (one per date for the next year):
```java
// RoomServiceImpl
LocalDate today = LocalDate.now();
for (int i = 0; i < 365; i++) {
    Inventory inventory = Inventory.builder()
        .hotel(room.getHotel())
        .room(room)
        .date(today.plusDays(i))
        .totalCount(room.getTotalCount())
        .bookedCount(0)
        .reservedCount(0)
        .surgeFactor(BigDecimal.ONE)
        .price(room.getBasePrice())
        .city(room.getHotel().getCity())
        .closed(false)
        .build();
}
```

---

## ⭐ 11. Reviews System

- A guest can only review a room **after a COMPLETED booking** for that room
- `GET /reviews/has-completed-booking/{roomId}` — eligibility check endpoint
- Reviews support: `rating`, `comment`, optional `photos`
- Paginated listing with sort options
- Only the review author can edit/delete their review

---

## 🤖 12. AI Integration (Spring AI + Gemini)

- Uses **Spring AI** with **Google Gemini 2.5 Flash** model
- Configured via `spring.ai.google.genai.api-key` and `spring.ai.google.genai.chat.options.model`
- `AiService` interface → `AiServiceImpl` → calls Gemini via `ChatClient`
- Exposed via controller in `controllers/ai/`

---

## ⚙️ 13. Configuration & Profiles

### Two Profiles

| Profile | DB | Notes |
|---|---|---|
| `dev` | localhost:5432/AtithiStay_db | Gemini API key from env var |
| `prod` | Env-var driven (`DB_HOST_URL`, etc.) | No hardcoded secrets |

### Key Properties (dev)

```properties
spring.jpa.hibernate.ddl-auto=update
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.ai.google.genai.chat.options.model=gemini-2.5-flash
frontend.url=http://localhost:8080
```

### Environment Variables Required

```
SPRING_PROFILES_ACTIVE=dev
STRIPE_SECRET=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
JWT_SECRET=your_jwt_secret_key
GEMINI_API_KEY=your_google_ai_key
```

---

## 🔑 14. Flyway Database Migrations

- Migration files in `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
  - `V1__initial_schema.sql` — creates all tables
  - `V2__update_booking_status_constraint.sql` — adds EXPIRED status
- Flyway tracks applied migrations in `flyway_schema_history` table
- `baseline-on-migrate=true` — handles pre-existing DBs gracefully

---

## 🎯 15. Design Patterns Used

| Pattern | Where Used | Purpose |
|---|---|---|
| **Decorator** | `PricingStrategy` chain in `strategy/` | Composable pricing layers |
| **Strategy** | `PricingStrategy` interface | Interchangeable algorithms |
| **Builder** | `Booking.builder()`, `SessionCreateParams.builder()` | Fluent object construction |
| **Repository** | Spring Data JPA repositories | Data access abstraction |
| **DTO** | All request/response objects | Decouple API from entity |
| **Filter Chain** | `JwtAuthFilter` | Pre-process all HTTP requests |
| **Singleton** | All Spring `@Service`, `@Repository` beans | Managed by Spring IoC |
| **Facade** | `BookingService` | Single entry point for complex booking logic |

---

## ❓ 16. Likely Interview Questions & Answers

### Q1: Explain the project in 2 minutes.

> "AtithiStay is a Spring Boot backend for a hotel booking platform. It supports two roles — guests and hotel managers. Guests can search hotels, create bookings, pay via Stripe, and leave reviews. Managers can manage hotels, rooms, and view reports. The backend uses JWT for stateless auth, Flyway for DB migrations, a dynamic pricing engine that runs every 10 minutes using the Decorator pattern, and integrates Google Gemini AI for chat assistance."

---

### Q2: How does JWT authentication work in your project?

> "On login, `AuthService` authenticates the user using `AuthenticationManager`, then calls `JwtService` to generate two tokens — an access token (contains userId, email, roles) and a refresh token (contains only userId). The access token is returned in the response body, and the refresh token is stored in an HttpOnly cookie. On every subsequent request, `JwtAuthFilter` intercepts it before the controller, extracts the Bearer token, validates the signature using HMAC-SHA, extracts the userId, loads the `User` from DB, and sets the `SecurityContextHolder`. The session policy is `STATELESS` — no server-side sessions are maintained."

---

### Q3: How does the booking lifecycle work?

> "There are 6 steps: First, the guest initialises a booking — rooms are reserved by incrementing `reservedCount` in inventory using a pessimistic lock. Second, the guest adds traveller details. Third, the guest requests payment — we create a Stripe Checkout session and set status to `PAYMENT_PENDING`. Fourth, Stripe calls our webhook after successful payment, we verify the signature, confirm the booking, and move `reservedCount` to `bookedCount`. Fifth, the hotel manager checks the guest in. Finally, a scheduled job auto-completes bookings after check-out. If the guest never pays within 10 minutes, another scheduler expires the booking and releases the inventory."

---

### Q4: How does the Dynamic Pricing Engine work?

> "I used the Decorator design pattern. There's a `PricingStrategy` interface with one method `calculatePrice(Inventory)`. `BasePricingStrategy` is the concrete base — it returns the room's base price. Then I wrap it with additional strategies: Surge (applies a surge multiplier from inventory), Occupancy (adds 20% if >80% occupied), Urgency (adds 15% if the date is within 7 days), and Holiday (premium on public holidays). A scheduled job `PricingUpdateService` runs every 10 minutes, processes all hotels in batches of 100, recalculates prices for all 365 future days per hotel, and updates `hotel.startingPrice` for display on the browse page."

---

### Q5: How do you handle race conditions in booking?

> "When a guest initialises a booking, I use `findAndLockAvailableInventory()` — a custom JPQL query with `SELECT ... FOR UPDATE` which acquires a pessimistic write lock on the inventory rows. This prevents two users from booking the same last room simultaneously. Only after locking do we check availability and increment `reservedCount`."

---

### Q6: What is Flyway and why did you use it?

> "Flyway is a database migration tool. Instead of using JPA's `ddl-auto=create`, I define SQL scripts like `V1__initial_schema.sql`. On startup, Flyway checks the `flyway_schema_history` table, finds which migrations haven't been applied yet, and runs them in order. This gives me a version-controlled, reproducible database schema that works consistently across dev, staging, and production. `baseline-on-migrate=true` ensures it works even on pre-existing databases."

---

### Q7: How is the Stripe webhook secured?

> "Stripe signs every webhook payload using HMAC-SHA256 with the `STRIPE_WEBHOOK_SECRET`. In our `WebhookController`, we call `Webhook.constructEvent(payload, sigHeader, endpointSecret)` which recomputes the signature and throws `SignatureVerificationException` if it doesn't match. This prevents any malicious actor from sending fake payment success events to our endpoint."

---

### Q8: What is ModelMapper and why use it?

> "ModelMapper automatically maps fields between objects with matching names and types. I use it to convert `User` entity → `UserDto`, `Booking` entity → `BookingDto`, etc. This avoids writing repetitive getter/setter mapping code. It's configured as a Spring `@Bean` in the config package."

---

### Q9: What are the roles in your system and how are they enforced?

> "There are two roles: `GUEST` and `HOTEL_MANAGER`. They're stored in the `User.roles` field (a `Set<Role>`). In `WebSecurityConfig`, I use `hasRole()` matchers — `/api/v1/admin/**` requires `HOTEL_MANAGER`, `POST /api/v1/rooms/*/reviews` requires `GUEST`. The roles are embedded in the JWT access token and Spring Security reads them from the `SecurityContext` on every request."

---

### Q10: How do you handle exceptions globally?

> "I use a `@ControllerAdvice` class in the `advice/` package. It has `@ExceptionHandler` methods for custom exceptions like `ResourceNotFoundException`, `UnAuthorisedException`, `BookingExpiredException`, and a catch-all for `RuntimeException`. Each handler returns a standardised error response with HTTP status code and message."

---

### Q11: What is the `@Transactional` annotation used for?

> "It ensures that all DB operations within a method happen in a single ACID transaction. For example, in `initialiseBooking()`, I lock inventory, check availability, increment `reservedCount`, and create the `Booking` — all in one transaction. If anything fails, everything rolls back, preventing partial state. I use `Propagation.REQUIRES_NEW` in `BookingExpirationManager` so it creates a new independent transaction even when called from within another."

---

### Q12: How do scheduled tasks work in Spring Boot?

> "I use `@EnableScheduling` on the main application class and `@Scheduled(cron = "...")` on methods. The cron format is: `second minute hour day month weekday`. For example, `0 */10 * * * *` runs every 10 minutes. I have three scheduled jobs: `updatePrice()` in `PricingUpdateService` (every 10 min), `expireBookings()` in `BookingServiceImpl` (every 10 min), and `updateCompletedBookings()` (every 1 min to mark checked-out bookings as COMPLETED)."

---

### Q13: What is the difference between `bookedCount` and `reservedCount` in Inventory?

> "`reservedCount` is incremented when a booking is initialised (status: RESERVED) — rooms are temporarily held. `bookedCount` is incremented only after **payment is confirmed** via the Stripe webhook. This two-phase approach prevents overbooking: even before payment, rooms are locked for 10 minutes. When a booking expires or is cancelled, `reservedCount` is decremented. `totalCount - bookedCount - reservedCount` gives the currently available rooms."

---

### Q14: Why did you use Spring AI instead of calling Gemini API directly?

> "Spring AI provides a vendor-agnostic abstraction layer. The `ChatClient` interface works the same way whether the underlying model is Gemini, OpenAI, or Ollama. This means I can swap models by changing a single property — `spring.ai.google.genai.chat.options.model` — without touching Java code. The `application-dev.properties` even has the commented-out Ollama config showing this flexibility."

---

### Q15: How is the project structured for production deployment?

> "There's a `buildspec.yml` for AWS CodeBuild CI/CD. For production, `SPRING_PROFILES_ACTIVE=prod` is set, and `application-prod.properties` reads all credentials (DB host, DB password, Stripe keys, JWT secret) from environment variables — nothing is hardcoded. The app is fully containerisable as a Spring Boot fat JAR."

---

## 📊 17. API Endpoints Quick Reference

### Auth — `/api/v1/auth`
| Method | Endpoint | Access |
|---|---|---|
| POST | `/signup` | Public |
| POST | `/login` | Public |
| POST | `/refresh` | Public |

### Browse — `/api/v1/hotels`
| Method | Endpoint | Access |
|---|---|---|
| POST | `/search` | Public |
| GET | `/` | Public |
| GET | `/{hotelId}/info` | Public |
| GET | `/{hotelId}/rooms` | Public |

### Booking — `/api/v1/bookings`
| Method | Endpoint | Access |
|---|---|---|
| POST | `/init` | Authenticated |
| POST | `/{id}/addGuests` | Authenticated |
| POST | `/{id}/payment` | Authenticated |
| POST | `/{id}/cancel` | Authenticated |
| POST | `/{id}/status` | Authenticated |
| GET | `/myBookings` | Authenticated |
| POST | `/{id}/check-in` | HOTEL_MANAGER |

### Admin — `/api/v1/admin/hotels`
| Method | Endpoint | Access |
|---|---|---|
| POST | `/` | HOTEL_MANAGER |
| GET | `/` | HOTEL_MANAGER |
| PUT | `/{hotelId}` | HOTEL_MANAGER |
| PATCH | `/{hotelId}` | HOTEL_MANAGER |
| DELETE | `/{hotelId}` | HOTEL_MANAGER |
| PATCH | `/activate/{hotelId}` | HOTEL_MANAGER |
| GET | `/{hotelId}/reports` | HOTEL_MANAGER |

### Webhook
| Method | Endpoint | Description |
|---|---|---|
| POST | `/webhook` | Stripe payment confirmation |

---

## ✅ 18. Quick Revision Checklist

Before your interview, make sure you can explain each of these confidently:

- [ ] Project overview in 2 minutes
- [ ] JWT access token vs refresh token flow
- [ ] Role-based access control setup
- [ ] Booking 6-step lifecycle and status transitions
- [ ] How inventory locking prevents overbooking (pessimistic lock)
- [ ] Dynamic pricing — Decorator pattern, all 5 strategies
- [ ] Stripe Checkout session creation
- [ ] Stripe webhook verification and payment capture
- [ ] Stripe refund on cancellation
- [ ] `@Scheduled` jobs — what they do and cron syntax
- [ ] Flyway migrations — why and how
- [ ] `@Transactional` — purpose and `REQUIRES_NEW` propagation
- [ ] ModelMapper — purpose
- [ ] Spring AI + Gemini integration
- [ ] Global exception handling with `@ControllerAdvice`
- [ ] `Inventory` entity — all fields explained
- [ ] Two Spring profiles: `dev` vs `prod`
- [ ] `buildspec.yml` → AWS CodeBuild deployment

---

> **💡 Pro Tip:** When asked "tell me about your project," always start with: **What it does → Who uses it → Key technical challenges you solved.** Then let the interviewer guide the depth of the conversation.

---

*Generated on 2026-07-01 for AtithiStay Backend — Spring Boot Interview Preparation*
