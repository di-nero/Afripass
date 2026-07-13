CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
    CHECK (role IN ('USER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    booking_status VARCHAR(50) NOT NULL
    CHECK (booking_status IN ('PENDING', 'CONFIRMED', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);


CREATE TABLE event_inventory (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL UNIQUE,
    available_seats INT NOT NULL CHECK (available_seats >= 0)
);


CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_number VARCHAR(255) NOT NULL UNIQUE,
    event_name VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    booking_status VARCHAR(50) NOT NULL
    CHECK (booking_status IN ('PENDING', 'CONFIRMED', 'EXPIRED')),
    booking_id BIGINT NOT NULL,
    issued_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_ticket_booking
        FOREIGN KEY (booking_id)
        REFERENCES booking(id)
);

CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_booking_event
ON booking(event_id);

CREATE INDEX idx_ticket_number
ON tickets(ticket_number);

CREATE INDEX idx_inventory_event
ON event_inventory(event_id);