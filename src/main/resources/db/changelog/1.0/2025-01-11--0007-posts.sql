--liquibase formatted sql

--changeset orlov:2025-01-11--0007-posts
CREATE TABLE posts
(
    id         BIGSERIAL PRIMARY KEY,
    content    VARCHAR(1000) NOT NULL,
    created_at timestamp     NOT NULL,
    user_id    BIGINT        NOT NULL,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);
