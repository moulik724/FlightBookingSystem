CREATE DATABASE IF NOT EXISTS flight_booking_system;
USE flight_booking_system;

-- Users Table: Stores user details like username, password, and role
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'customer','manager') NOT NULL
);

-- Flights Table: Stores flight information including flight number, origin, destination, etc.
CREATE TABLE IF NOT EXISTS flights (
    flightNumber VARCHAR(6) PRIMARY KEY,
    origin VARCHAR(100),
    destination VARCHAR(100),
    departure_time DATETIME,
    total_seats INT,
    available_seats INT,
    cost DECIMAL(10,2),
    approved BOOLEAN DEFAULT FALSE
);

-- Bookings Table: Stores the booking details with the relevant foreign keys and new fields
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(6),
    user_id INT,
    num_seats INT,
    booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_name VARCHAR(100),
    dob DATE,
    phone VARCHAR(15),
    email VARCHAR(100),
    address TEXT,
    seat_number VARCHAR(255),
    extra_passengers VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (flight_number) REFERENCES flights(flightNumber) ON DELETE CASCADE
);

