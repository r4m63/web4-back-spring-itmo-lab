DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS points;
DROP TABLE IF EXISTS password_reset_token;

DELETE
FROM users;
DELETE
FROM refresh_token;
DELETE
FROM points;
DELETE
FROM password_reset_token;


CREATE TABLE IF NOT EXISTS users
(
    id                 SERIAL PRIMARY KEY,
    email              VARCHAR(255) UNIQUE,
    username           TEXT,
    password_hash      TEXT,
    is_verified        BOOLEAN     DEFAULT FALSE,
    verification_token TEXT UNIQUE,
    is_google_auth     BOOLEAN     DEFAULT FALSE,
    avatar_url         TEXT        DEFAULT NULL,
    role               VARCHAR(50) DEFAULT 'USER', -- Роли: 'USER', 'ADMIN'
    created_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_token
(
    id         SERIAL PRIMARY KEY,
    token      TEXT,
    user_id    INT REFERENCES users (id) ON DELETE CASCADE,
    device_on  TEXT, -- UUID
    is_revoked BOOLEAN   DEFAULT FALSE,
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS points
(
    id         SERIAL PRIMARY KEY,
    user_id    INT REFERENCES users (id) ON DELETE CASCADE,
    x          FLOAT,
    y          FLOAT,
    r          FLOAT,
    res        BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_token
(
    id         SERIAL PRIMARY KEY,
    user_id    INT REFERENCES users (id) ON DELETE CASCADE,
    token      TEXT,
    is_used    BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);