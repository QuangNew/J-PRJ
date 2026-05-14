-- ============================================================
-- BusEasy Admin Account
-- Run AFTER schema.sql. Safe to run more than once.
--
-- Login:
--   username: admin
--   password: admin123
-- ============================================================

USE buseasy;

-- Existing databases created before the admin feature need the role column.
SET @has_role := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role'
);

SET @role_sql := IF(
    @has_role = 0,
    'ALTER TABLE users ADD COLUMN role ENUM(''USER'',''ADMIN'') NOT NULL DEFAULT ''USER'' AFTER is_military',
    'SELECT ''users.role already exists'''
);

PREPARE role_stmt FROM @role_sql;
EXECUTE role_stmt;
DEALLOCATE PREPARE role_stmt;

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

-- SHA-256(admin123) = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO users (username, password_hash, full_name, email, phone, is_military, role)
VALUES (
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'BusEasy Administrator',
    'admin@buseasy.local',
    '0900000000',
    FALSE,
    'ADMIN'
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name     = VALUES(full_name),
    phone         = VALUES(phone),
    is_military   = VALUES(is_military),
    role          = 'ADMIN';
