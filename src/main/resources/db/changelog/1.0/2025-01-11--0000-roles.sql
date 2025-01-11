-- Создание таблицы ролей
--changeset orlov:2025-01-11--0000-roles-table
CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL UNIQUE
);
