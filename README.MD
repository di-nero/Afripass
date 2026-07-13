# AfriPass

A backend ticket booking platform built with Spring Boot that allows users to discover events, reserve seats, securely make payments, and receive digital tickets.

## Features

- User authentication with JWT
- Browse available events
- Reserve seats with pessimistic locking
- Booking expiration scheduler
- Stripe payment integration
- Digital ticket generation
- RabbitMQ event publishing
- Redis caching
- Rate limiting with Bucket4j
- Dockerized deployment
- Unit testing with JUnit and Mockito

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- PostgreSQL
- Redis
- RabbitMQ
- Stripe
- Flyway
- Docker & Docker Compose
- JUnit 5
- Mockito
- Maven

## Architecture

The project follows a layered architecture:

Controller
↓
Service
↓
Repository
↓
PostgreSQL

External integrations:

- Stripe (payments)
- RabbitMQ (messaging)
- Redis (caching)

## Project Structure

src/main/java
├── config
├── controller
├── service
├── repository
├── model
├── dto
├── security
├── client
├── exception
├── interceptor
└── util

## Key Features

### Event Booking

Users can reserve seats for events. Pessimistic locking prevents overselling when multiple users attempt to book simultaneously.

### Booking Expiration

Pending bookings automatically expire after 15 minutes, and reserved seats are returned to inventory.

### Payments

Payments are processed securely through Stripe.

The application includes:

- Retry
- Circuit Breaker
- Payment fallback

using Resilience4j.

### Ticket Generation

Successful payments generate unique digital tickets for each attendee.

### Messaging

RabbitMQ publishes booking confirmation events for asynchronous processing.

## Testing

The project includes unit tests for the service layer using:

- JUnit 5
- Mockito

Run all tests:

```bash
./mvnw test
```

## Running the Project

### Using Docker

```bash
docker compose up --build
```

### Running locally

```bash
./mvnw spring-boot:run
```

## Future Improvements

- Email ticket delivery
- QR code validation
- Admin dashboard
- Event creation portal
- Kubernetes deployment
- CI/CD pipeline

## Author

Adeyemo Opeyemi

GitHub:
https://github.com/di-nero