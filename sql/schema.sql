-- ============================================================
-- BusEasy Database Schema
-- Run this file once before starting the application.
-- XAMPP MySQL: open phpMyAdmin or mysql CLI and run this file.
-- ============================================================

CREATE DATABASE IF NOT EXISTS buseasy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE buseasy;

-- ------------------------------------------------------------
-- 1. users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    is_military   BOOLEAN      NOT NULL DEFAULT FALSE,
    role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 2. buses
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS buses (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    bus_number   VARCHAR(20)  NOT NULL UNIQUE,
    bus_name     VARCHAR(100) NOT NULL,
    total_seats  INT          NOT NULL
);

-- ------------------------------------------------------------
-- 3. routes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS routes (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    start_destination VARCHAR(100) NOT NULL,
    end_destination   VARCHAR(100) NOT NULL
);

-- ------------------------------------------------------------
-- 4. bus_schedules
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bus_schedules (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    bus_id           INT           NOT NULL,
    route_id         INT           NOT NULL,
    departure_time   DATETIME      NOT NULL,
    arrival_time     DATETIME      NOT NULL,
    price_adult      DECIMAL(10,2) NOT NULL,
    available_seats  INT           NOT NULL,
    status           ENUM('ACTIVE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (bus_id)   REFERENCES buses(id),
    FOREIGN KEY (route_id) REFERENCES routes(id)
);

-- ------------------------------------------------------------
-- 5. cart_items
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cart_items (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT     NOT NULL,
    schedule_id  INT     NOT NULL,
    qty_adult    INT     NOT NULL DEFAULT 0,
    qty_child    INT     NOT NULL DEFAULT 0,
    is_military  BOOLEAN NOT NULL DEFAULT FALSE,
    added_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES bus_schedules(id)
);

-- ------------------------------------------------------------
-- 6. tickets
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT           NOT NULL,
    schedule_id  INT           NOT NULL,
    qty_adult    INT           NOT NULL DEFAULT 0,
    qty_child    INT           NOT NULL DEFAULT 0,
    is_military  BOOLEAN       NOT NULL DEFAULT FALSE,
    total_price  DECIMAL(10,2) NOT NULL,
    status       ENUM('VALID','EXPIRED','CANCELLED') NOT NULL DEFAULT 'VALID',
    purchased_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES users(id),
    FOREIGN KEY (schedule_id) REFERENCES bus_schedules(id)
);

-- ------------------------------------------------------------
-- 7. ticket_reminders
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ticket_reminders (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id      INT      NOT NULL,
    user_id        INT      NOT NULL,
    offset_minutes INT      NOT NULL,
    remind_at      DATETIME NOT NULL,
    delivered_at   DATETIME NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ticket_reminders_ticket_id (ticket_id),
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 8. notifications
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT          NOT NULL,
    title      VARCHAR(120) NOT NULL,
    message    TEXT         NOT NULL,
    type       VARCHAR(40)  NOT NULL DEFAULT 'INFO',
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 9. military_requests
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS military_requests (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    service_number  VARCHAR(60)  NOT NULL,
    unit_name       VARCHAR(120) NOT NULL,
    note            TEXT,
    status          ENUM('PENDING','APPROVED','DENIED') NOT NULL DEFAULT 'PENDING',
    admin_note      TEXT,
    reviewed_by     INT NULL,
    reviewed_at     DATETIME NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);
