-- Создание таблицы interests
--changeset orlov:2025-01-11--0001-interests-table
CREATE TABLE interests
(
    id          BIGSERIAL PRIMARY KEY,
    description VARCHAR(30)
);
