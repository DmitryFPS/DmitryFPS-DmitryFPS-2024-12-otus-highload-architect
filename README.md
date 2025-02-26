# Учебный проект: Социальная сеть

Этот проект представляет собой учебное приложение социальной сети, разработанное с использованием современных
технологий. Весь стек технологий можно узнать в файле `pom.xml`.

---

## Как запустить приложение

### 1. Настройка системных переменных (Environment Variables)

Для корректной работы приложения необходимо настроить системные переменные:

1. Перейдите в **Edit Configurations**.
2. Выберите ваше приложение (**Application**).
3. Перейдите в **Modify options**.
4. Выберите **Environment Variables**.
5. Заполните переменные:

    - **JWT_EXPIRATION**: Срок действия токена (в секундах).  
      Пример: `86400` (24 часа).
    - **JWT_SECRET**: Секретный ключ для генерации JWT.  
      Пример: `+AVVLHD+9HxbBZYQmEnnuwisUGzW/m89H7i5FMkHEqE=`.  
      Вы можете использовать этот ключ или сгенерировать свой с помощью теста `SecretKeyGeneratorTest` (алгоритм
      HMAC-SHA256).
    - **PASSWORD_BD**: Пароль для подключения к базе данных.  
      Пример: `root`.
    - **USER_NAME_BD**: Имя пользователя для подключения к базе данных.  
      Пример: `root`.

---

### 2. Запуск Docker Compose

Запустите приложение с помощью Docker Compose:

```bash
docker compose up -d
```

Этот скрипт выполняет следующие действия:

- Поднимает **PostgreSQL** (база данных).
- Собирает приложение в Docker-контейнер (в разработке, контейнер может падать).
- Теперь запускаем приложение жмякаем run

---

## Работа с приложением через Postman

### 3. Получение JWT Token

Для получения токена выполните следующие шаги:

1. Отправьте POST-запрос на эндпоинт:  
   `POST http://localhost:8080/api/v1/login`
2. В теле запроса (Body -> raw -> JSON) укажите:
   ```json
   {
       "username": "ddd12@mail.ru",
       "password": "root"
   }
   ```
3. В ответе вы получите JWT Token.

---

### 4. Получение пользователя по ID

Для получения информации о пользователе выполните следующие шаги:

1. Отправьте GET-запрос на эндпоинт:  
   `GET http://localhost:8080/api/v1/user/{id}`
2. В разделе **Authorization** выберите тип авторизации **Bearer Token**.
3. Вставьте токен, полученный на предыдущем шаге.

---

### 5. Регистрация нового пользователя

Для регистрации нового пользователя выполните следующие шаги:

1. Отправьте POST-запрос на эндпоинт:  
   `POST http://localhost:8080/api/v1/user/register`
2. В теле запроса (Body -> raw -> JSON) укажите данные пользователя:
   ```json
   {
     "firstName": "Olya",
     "lastName": "Orlova",
     "birthDate": "1995-12-11T21:00:00.000+00:00",
     "gender": "FEMALE",
     "interests": ["It", "Moto", "Other"],
     "city": "Ulyanovsk",
     "email": "olya111@mail.ru",
     "password": "super",
     "role": ["USER", "ADMIN"]
   }
   ```
3. Токен для этого запроса не требуется.

---

### 6. Обновление Токена

Для обновления старого токена выполните следующие шаги:

1. Отправьте POST-запрос на эндпоинт:  
   `POST http://localhost:8080/api/v1/refresh`
2. В теле запроса (Body -> raw -> JSON) укажите данные пользователя:
   ```json
   {
     "refreshToken": ""
   }
   ```
3. В разделе **Authorization** выберите тип авторизации **Bearer Token**. Укажите тот же токен что и в Body

---

### 7. Logout

Для logout выполните следующие шаги:

1. Отправьте POST-запрос на эндпоинт:  
   `POST http://localhost:8080/api/v1/logout`
2. В теле запроса (Body -> raw -> JSON) укажите данные пользователя:
   ```json
   {
     "email": "ddd12@mail.ru"
   }
   ```

---

### 8. Postman-коллекция

Для удобства тестирования API в проекте доступна Postman-коллекция. Ее можно найти в директории `postman`.

---

## Структура проекта

- **`pom.xml`**: Файл конфигурации Maven, содержащий список зависимостей и используемых технологий.
- **`src/main/java`**: Исходный код приложения.
- **`src/test/java`**: Тесты для приложения.
- **`postman`**: Коллекция Postman для тестирования API.

---

## Технологии

- **Java**: Основной язык разработки.
- **Spring Boot**: Фреймворк для создания приложения.
- **PostgreSQL**: База данных.
- **Docker**: Контейнеризация приложения.
- **JWT (JSON Web Token)**: Аутентификация и авторизация.
- **Postman**: Тестирование API.

---

## Генерация 1_000_000 анкет

- Запускаем GenerateCSV будет сгенерированы 3 файла в /resources/migration
  users.csv, user_roles.csv, user_interests.csv
- Можно на уникальность проверить csv
    - sort users.csv | uniq -d
    - sort user_roles.csv | uniq -d
    - sort user_interests.csv | uniq -d
- Далее запускаем приложение и они сгенерирует начальные схемы
- Далее запускаем docker-compose.yml
- Заходим в контейнер docker exec -it CONTAINER ID bash
- Заходим в psql -h postgres -p 5432 -U root -d db (По просьбе вводим пароль от БД 'root')
- Проверяем что находимся там где нужно Пример одной схемы \dt public.users
- Выполняем быструю вставку больших объемов данных:
    - \copy users (first_name, last_name, birth_date, gender, email, password, is_active, city_id) FROM '/data/users.csv'
      WITH (FORMAT csv, HEADER);
    - \copy user_roles (user_id, role_id) FROM '/data/user_roles.csv' WITH (FORMAT csv, HEADER);
    - \copy user_interests (user_id, interest_id) FROM '/data/user_interests.csv' WITH (FORMAT csv, HEADER);
- Идем в БД и проверяем что все хорошо

## Проверка, что в файлах полное кол-во данных (IDEA почему то не всегда показывает больше 12 000 строк)

- wc -l users.csv
- wc -l user_interests.csv
- wc -l user_roles.csv

## Для нагрузочного тестирования поиска анкет по префиксу имени и фамилии (одновременно)

- Запрос в форме firstName LIKE ? and secondName LIKE ?
- Сортировать вывод по id анкеты
- GET /user/search?first_name=Конст&last_name=Оси

## Отчет по тестированию производительности запросов до и после добавления индексов
[Отчет: ](./src/main/java/ru/otus/orlov/docs/loadtestingreport/report.md)

---

# Репликация
[Отчет: ](src/main/java/ru/otus/orlov/docs/replication/report.md)

Если у вас есть вопросы или предложения, пожалуйста, создайте issue или свяжитесь с автором проекта.
