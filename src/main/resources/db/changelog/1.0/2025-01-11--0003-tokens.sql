-- Создание таблицы interests
--changeset orlov:2025-01-11--0003-tokens-table
CREATE TABLE tokens
(
    id                       BIGSERIAL PRIMARY KEY,
    refresh_token            VARCHAR(255),
    access_token_expiration  timestamp,
    refresh_token_expiration timestamp
);
