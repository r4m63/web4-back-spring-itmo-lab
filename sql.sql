CREATE TABLE users
(
    id                 SERIAL PRIMARY KEY,
    email              VARCHAR(255) UNIQUE NOT NULL,
    password_hash      TEXT                NOT NULL,
    salt               TEXT                NOT NULL,
    is_verified        BOOLEAN                      DEFAULT FALSE,
    verification_token TEXT UNIQUE,
    avatar_url         TEXT                         DEFAULT NULL,
    role               VARCHAR(50)         NOT NULL DEFAULT 'USER', -- Роли: 'USER', 'ADMIN'
    created_at         TIMESTAMP                    DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP                    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token
(
    id         SERIAL PRIMARY KEY,
    token_hash TEXT UNIQUE NOT NULL,
    user_id    INT REFERENCES users (id) ON DELETE CASCADE,
    device_on  TEXT, -- UUID
    is_revoked BOOLEAN   DEFAULT FALSE,
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP   NOT NULL,
);

