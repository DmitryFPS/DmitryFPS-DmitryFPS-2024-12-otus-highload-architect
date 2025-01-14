--liquibase formatted sql

--changeset orlov:2025-01-11--0004-users-table
CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE');

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    birth_date DATE         NOT NULL,
    gender     gender_enum  NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE,
    city_id    BIGINT       NOT NULL,
    token_id   BIGINT,

    CONSTRAINT fk_city FOREIGN KEY (city_id) REFERENCES cities (id),
    CONSTRAINT fk_token FOREIGN KEY (token_id) REFERENCES tokens (id)
);

-- -- Индексы для внешних ключей
-- CREATE INDEX idx_users_city_id ON users (city_id);
-- CREATE INDEX idx_users_token_id ON users (token_id);
--
-- -- Индексы для фильтрации
-- CREATE INDEX idx_users_is_active ON users (is_active);
-- CREATE INDEX idx_users_gender ON users (gender);
--
-- -- Составной индекс для gender и is_active
-- CREATE INDEX idx_users_gender_is_active ON users (gender, is_active);
--
-- -- Индексы для поиска по имени и фамилии
-- CREATE INDEX idx_users_first_name ON users (first_name);
-- CREATE INDEX idx_users_last_name ON users (last_name);
--
-- -- Индекс для сортировки по дате рождения
-- CREATE INDEX idx_users_birth_date ON users (birth_date);
