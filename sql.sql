DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS refresh_token;

DELETE
FROM users;
DELETE
FROM refresh_token;


CREATE TABLE IF NOT EXISTS users
(
    id                 SERIAL PRIMARY KEY,
    email              VARCHAR(255) UNIQUE,
    password_hash      TEXT,
    is_verified        BOOLEAN     DEFAULT FALSE,
    verification_token TEXT UNIQUE,
    is_google_auth     BOOLEAN,
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

