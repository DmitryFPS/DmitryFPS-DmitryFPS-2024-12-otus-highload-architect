-- Создание таблицы юзеров и ролей many-to-many
--changeset orlov:2025-01-11--0005-user-interests-table
CREATE TABLE user_interests
(
    user_id     BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, interest_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE
);
