-- Создание таблицы cities
--changeset orlov:2025-01-11--0001-cities-table
CREATE TABLE cities
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
