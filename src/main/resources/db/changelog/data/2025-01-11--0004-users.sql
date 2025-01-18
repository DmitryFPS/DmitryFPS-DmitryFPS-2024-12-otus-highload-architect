--changeset orlov:2025-01-11--0004-users
-- password это слово root (только захэшированный)
insert into users (first_name, last_name, birth_date, gender, email, password, is_active, city_id)
values ('Dima', 'Orlov', '1995-12-12', 'MALE', 'ddd12@mail.ru',
        '$2a$12$Ec9W/LSQsG7DrmSwY0nS/.FBK2E5KPtBciW81NaVV4PTQL1WVHXPO', true, 1);
