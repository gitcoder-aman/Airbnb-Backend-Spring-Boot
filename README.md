# 🏨 AtithiStay Backend — Airbnb Clone

> A production-ready, full-featured hotel booking backend built with **Spring Boot 4**, **PostgreSQL**, **JWT Auth**, **Stripe Payments**, and **Google Gemini AI**.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [API Endpoints](#-api-endpoints)
- [Dynamic Pricing Engine](#-dynamic-pricing-engine)
- [Security](#-security)
- [Environment Variables](#-environment-variables)
- [Getting Started](#-getting-started)
- [Deployment](#-deployment)

---

## 🌐 Overview

**AtithiStay** is a full-stack Airbnb-clone backend that powers a hotel discovery and booking platform. The system supports two roles — **Guests** (travellers) and **Hotel Managers** (admins) — enabling a complete end-to-end booking lifecycle: search → reserve → pay → check-in → review.

Key highlights:
- **Role-based access control** via JWT (Guest / Hotel Manager)
- **Stripe Checkout** for real payment sessions
- **Dynamic pricing** recalculated every 10 minutes using 4 pluggable strategies
- **Google Gemini AI** integration for intelligent assistance
- **Flyway** for versioned database migrations
- **Swagger UI** for interactive API documentation

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.5 |
| Language | Java 21 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Payments | Stripe Java SDK 28.2.0 |
| AI | Spring AI + Google Gemini 2.5 Flash |
| Mapping | ModelMapper 3.2.2 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Boilerplate | Lombok |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      REST API Layer                          │
│  AuthController  │  HotelBrowseController  │  BookingCtrl   │
│  ReviewController│  HotelAdminController   │  WebhookCtrl   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                     Service Layer                            │
│  BookingService │ HotelService │ InventoryService            │
│  ReviewService  │ PricingUpdateService (Scheduled)           │
│  CheckoutService│ AiService │ BookingExpirationManager       │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   Strategy Pattern                           │
│  BasePricing │ OccupancyPricing │ HolidayPricing             │
│  SurgePricing│ UrgencyPricing                                │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│               Repository Layer (Spring Data JPA)             │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                  PostgreSQL Database                          │
│      Flyway Versioned Migrations (V1, V2 ...)                │
└─────────────────────────────────────────────────────────────┘
```

---

## ✨ Features

### 👤 Authentication & Authorization
- User registration and login with encrypted passwords (BCrypt)
- **Access Token** (JWT, short-lived) + **Refresh Token** (HttpOnly cookie)
- Role-based access: `GUEST` and `HOTEL_MANAGER`

### 🏨 Hotel Management (Admin)
- CRUD operations on hotels (create, read, update, delete, patch)
- Activate / deactivate hotel listings
- View all hotels owned by the logged-in manager
- Hotel-level booking reports with optional date range filter

### 🔍 Hotel Discovery (Public / Guest)
- Search hotels by city, check-in/out dates, guest count with **paginated results**
- Browse all hotels (paginated)
- Get detailed hotel info including rooms and amenities
- Fetch available rooms for a hotel by date range

### 📦 Room & Inventory Management
- Create and delete rooms per hotel
- Auto-generated inventory entries for a 1-year window when a room is created
- Inventory tracks: total rooms, booked count, reserved count, surge factor, per-date price

### 📅 Booking Lifecycle
1. **Initialise Booking** — reserves rooms (deducts from inventory for the date range)
2. **Add Guests** — attach guest details to the booking
3. **Initiate Payment** — creates a Stripe Checkout session, returns the session URL
4. **Stripe Webhook** — confirms payment, finalises booking status to `CONFIRMED`
5. **Cancel Booking** — releases reserved inventory
6. **Check-In** — Hotel manager marks guest as checked in
7. **View My Bookings** — authenticated guest views their booking history

### 💰 Stripe Payment Integration
- Stripe Checkout session created server-side
- Webhook endpoint (`/webhook`) listens for `checkout.session.completed` events
- Booking status transitions: `PENDING → CONFIRMED`

### 💹 Dynamic Pricing Engine
Prices are automatically recalculated **every 10 minutes** using a chain of composable strategies:

| Strategy | Logic |
|---|---|
| `BasePricingStrategy` | Uses the room's base price as a floor |
| `OccupancyPricingStrategy` | Price increases as occupancy % rises |
| `HolidayPricingStrategy` | Premium pricing on public holidays |
| `SurgePricingStrategy` | Applies the inventory's surge factor |
| `UrgencyPricingStrategy` | Higher price for near-future dates |

The `PricingUpdateService` batch-processes all hotels, updates per-date inventory prices, and refreshes each hotel's `startingPrice` (the displayed minimum price).

### ⭐ Reviews
- Guests can submit reviews **only after a completed booking** for that room
- Paginated review listing with sort and filter (e.g., filter by photos)
- Edit and delete own reviews
- Check if current user is eligible to review a room

### 🤖 AI Assistant
- Google Gemini 2.5 Flash integrated via Spring AI
- AI chat endpoint powered by the `AiService`

---

## 📁 Project Structure

```
src/main/java/com/tech/project/AirbnbBackend/
│
├── AirbnbBackendApplication.java       # Entry point
│
├── controllers/
│   ├── admin/
│   │   ├── HotelAdminController.java   # Hotel CRUD, reports
│   │   ├── InventoryController.java    # Inventory management
│   │   ├── RoomController.java         # Room CRUD
│   │   └── HealthCheckController.java
│   ├── user/
│   │   ├── AuthController.java         # Signup, Login, Refresh
│   │   ├── HotelBrowseController.java  # Search, browse hotels
│   │   ├── HotelBookingController.java # Full booking lifecycle
│   │   ├── ReviewController.java       # Reviews CRUD
│   │   └── UserController.java
│   ├── ai/                             # AI chat controller
│   └── WebhookController.java          # Stripe webhook handler
│
├── entities/
│   ├── User.java
│   ├── Hotel.java
│   ├── Room.java
│   ├── Inventory.java
│   ├── Booking.java
│   ├── Guest.java
│   ├── Payment.java
│   ├── Review.java
│   ├── HotelContactInfo.java
│   ├── HotelMinPrice.java
│   └── enums/
│       ├── Role.java           # GUEST, HOTEL_MANAGER
│       ├── BookingStatus.java  # PENDING, CONFIRMED, CANCELLED, CHECKED_IN
│       ├── PaymentStatus.java
│       ├── Gender.java
│       └── RoomType.java
│
├── services/
│   ├── impl/                   # Service implementations
│   ├── BookingService.java
│   ├── HotelService.java
│   ├── InventoryService.java
│   ├── ReviewService.java
│   ├── RoomService.java
│   ├── UserService.java
│   ├── CheckoutService.java
│   ├── AiService.java
│   ├── PricingUpdateService.java       # Scheduled pricing job
│   └── BookingExpirationManager.java  # Expires stale bookings
│
├── strategy/
│   ├── PricingStrategy.java            # Interface
│   ├── PriceService.java               # Chains all strategies
│   ├── BasePricingStrategy.java
│   ├── OccupancyPricingStrategy.java
│   ├── HolidayPriceStrategy.java
│   ├── SurgePriceStrategy.java
│   └── UrgencyPricingStrategy.java
│
├── security/
│   ├── WebSecurityConfig.java          # Security filter chain & route rules
│   ├── JwtAuthFilter.java              # JWT validation filter
│   ├── JwtService.java                 # Token generation & validation
│   └── AuthService.java               # Login, signup, refresh logic
│
├── dto/                        # Request / Response DTOs
├── repositories/               # Spring Data JPA Repositories
├── advice/                     # Global exception handler
├── exception/                  # Custom exceptions
├── config/                     # App-level beans (ModelMapper, Stripe, etc.)
└── utils/                      # Utilities (e.g. getCurrentUser)

src/main/resources/
├── application.properties          # Shared config (profile, Stripe, JWT keys)
├── application-dev.properties      # Local dev (PostgreSQL localhost, Gemini AI)
├── application-prod.properties     # Production (env-var driven DB config)
└── db/migration/
    ├── V1__...sql                  # Initial schema
    └── V2__update_booking_status_constraint.sql
```

---

## 🗄 Database Schema

### Core Entities & Relationships

```
User (1) ──────────── (N) Hotel          [owner]
Hotel (1) ─────────── (N) Room
Hotel (1) ─────────── (N) Inventory
Room (1) ──────────── (N) Inventory
User (1) ──────────── (N) Booking        [guest]
Hotel (1) ─────────── (N) Booking
Room (1) ──────────── (N) Booking
Booking (M) ────────── (N) Guest         [booking_guests join table]
Hotel (1) ─────────── (N) HotelMinPrice  [daily min prices]
Room (1) ──────────── (N) Review
```

### Key Entity Fields

**Inventory** — the heart of availability & pricing:
- `hotel_id`, `room_id`, `date` — composite unique key
- `totalCount`, `bookedCount`, `reservedCount` — room availability
- `price` — dynamically updated every 10 min
- `surgeFactor` — multiplier applied by surge strategy
- `closed` — can manually close availability for a date

**Booking** — full lifecycle:
- `bookingStatus`: `PENDING → CONFIRMED → CHECKED_IN / CANCELLED`
- `totalAmount`, `subTotalAmount`, `taxAmount`
- `paymentSessionId` — links to Stripe session

---

## 📡 API Endpoints

### Auth — `/api/v1/auth`
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/signup` | Register a new user | Public |
| POST | `/login` | Login, returns access + refresh token | Public |
| POST | `/refresh` | Refresh access token via cookie | Public |

### Hotels (Public Browse) — `/api/v1/hotels`
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/search` | Search hotels by city, dates, guests | Public |
| GET | `/` | Get all hotels (paginated) | Public |
| GET | `/{hotelId}/info` | Get full hotel details | Public |
| GET | `/{hotelId}/rooms` | Get rooms by hotel & date range | Public |

### Bookings — `/api/v1/bookings`
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/init` | Create a new booking (reserve rooms) | Authenticated |
| POST | `/{bookingId}/addGuests` | Add guest details | Authenticated |
| POST | `/{bookingId}/payment` | Get Stripe checkout URL | Authenticated |
| POST | `/{bookingId}/cancel` | Cancel booking | Authenticated |
| POST | `/{bookingId}/status` | Get booking status | Authenticated |
| GET | `/myBookings` | View user's booking history | Authenticated |
| POST | `/{bookingId}/check-in` | Mark guest checked in | HOTEL_MANAGER |

### Reviews — `/api/v1`
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/rooms/{roomId}/reviews` | Create a review | GUEST |
| GET | `/reviews` | Get paginated reviews for a room | Public |
| PUT | `/reviews/{reviewId}` | Update own review | Authenticated |
| DELETE | `/reviews/{reviewId}` | Delete own review | Authenticated |
| GET | `/reviews/has-completed-booking/{roomId}` | Check review eligibility | Authenticated |

### Admin — `/api/v1/admin/hotels`
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/` | Create hotel | HOTEL_MANAGER |
| GET | `/` | Get all owned hotels | HOTEL_MANAGER |
| GET | `/{hotelId}` | Get hotel by ID | HOTEL_MANAGER |
| PUT | `/{hotelId}` | Full update hotel | HOTEL_MANAGER |
| PATCH | `/{hotelId}` | Partial update hotel | HOTEL_MANAGER |
| DELETE | `/{hotelId}` | Delete hotel | HOTEL_MANAGER |
| PATCH | `/activate/{hotelId}` | Activate hotel listing | HOTEL_MANAGER |
| GET | `/{hotelId}/bookings` | All bookings for hotel | HOTEL_MANAGER |
| GET | `/{hotelId}/reports` | Revenue report (date range) | HOTEL_MANAGER |

### Webhook — `/webhook`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Stripe event handler (payment confirmation) |

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

---

## 💹 Dynamic Pricing Engine

The `PricingUpdateService` runs on a **cron schedule every 10 minutes** and processes all hotels in batches of 100. For each hotel, it:

1. Fetches all inventory records for the next 365 days
2. Runs each inventory entry through the `PriceService` which **chains all strategies**:
   ```
   Base → Occupancy → Holiday → Surge → Urgency
   ```
3. Saves updated prices to the `inventory` table
4. Computes the minimum price per date → stores in `hotel_min_price`
5. Updates `hotel.startingPrice` with the global minimum

This ensures that the prices displayed to guests always reflect real-time demand, seasonality, and urgency.

---

## 🔐 Security

The security layer is configured in `WebSecurityConfig`:

| Route Pattern | Rule |
|---|---|
| `/api/v1/admin/**` | Requires `ROLE_HOTEL_MANAGER` |
| `/api/v1/auth/**` | Anonymous only (unauthenticated) |
| `/api/v1/bookings/**` | Authenticated |
| `/api/v1/users/**` | Authenticated |
| `POST /api/v1/rooms/*/reviews` | Requires `ROLE_GUEST` |
| `GET /api/v1/reviews/**` | Public |
| Everything else | Public |

- **Sessions**: Stateless (no server-side sessions)
- **Tokens**: Short-lived JWT access token + long-lived refresh token stored as HttpOnly cookie
- **Passwords**: BCrypt hashed

---

## 🔑 Environment Variables

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Set to `dev` or `prod` |
| `STRIPE_SECRET` | Stripe secret API key |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret |
| `JWT_SECRET` | Secret key for JWT signing |
| `GEMINI_API_KEY` | Google Gemini AI API key (dev profile) |
| `DB_HOST_URL` | PostgreSQL host (prod profile) |
| `DB_NAME` | Database name (prod profile) |
| `DB_USERNAME` | Database username (prod profile) |
| `DB_PASSWORD` | Database password (prod profile) |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+ (running locally)
- A Stripe account (test keys work fine)
- A Google AI Studio API key (for Gemini)

### 1. Clone the repository

```bash
git clone https://github.com/gitcoder-aman/Airbnb-Backend-Spring-Boot.git
cd Airbnb-Backend-Spring-Boot
```

### 2. Set up the database

```sql
CREATE DATABASE "AtithiStay_db";
```

> The default dev config expects PostgreSQL on `localhost:5432` with username `postgres`.

### 3. Configure environment variables

Create a `.env` file or export the following in your shell:

```env
SPRING_PROFILES_ACTIVE=dev
STRIPE_SECRET=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
JWT_SECRET=your_super_secret_jwt_key
GEMINI_API_KEY=your_gemini_api_key
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on **`http://localhost:8080`**.

### 5. Open Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

## ☁️ Deployment

The project includes a `buildspec.yml` for **AWS CodeBuild** CI/CD pipelines.

For production deployment, set `SPRING_PROFILES_ACTIVE=prod` and provide all production environment variables listed above. The `application-prod.properties` reads DB credentials from environment variables — no secrets are hardcoded.

### Flyway Migrations

Flyway is enabled for both profiles. On startup, it auto-applies any pending SQL migration scripts from:

```
src/main/resources/db/migration/
```

Migration files follow the naming convention: `V{version}__{description}.sql`

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

---

<div align="center">
  <strong>Built with ❤️ using Spring Boot | AtithiStay — Where every guest feels at home</strong>
</div>
