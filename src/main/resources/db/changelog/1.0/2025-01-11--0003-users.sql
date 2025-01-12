--liquibase formatted sql

--changeset orlov:2025-01-11--0003-users-table
CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE');

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    birth_date DATE         NOT NULL,
    gender     gender_enum  NOT NULL,
    email      VARCHAR(30)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE,
    city_id    BIGINT,

    CONSTRAINT fk_city FOREIGN KEY (city_id) REFERENCES cities (id)
);
