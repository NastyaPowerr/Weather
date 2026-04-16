CREATE SCHEMA IF NOT EXISTS weather_test;

CREATE TABLE IF NOT EXISTS weather_test.users
(
    id       SERIAL PRIMARY KEY,
    login    VARCHAR(50) UNIQUE,
    password VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS  weather_test.locations
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    user_id   INT,
    latitude  decimal,
    longitude decimal,
    CONSTRAINT fk_location_owner FOREIGN KEY (user_id) REFERENCES weather_test.users (id)
);

CREATE TABLE IF NOT EXISTS  weather_test.sessions
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    INT,
    expires_at TIMESTAMP,
    CONSTRAINT fk_session_owner FOREIGN KEY (user_id) REFERENCES  weather_test.users (id)
);