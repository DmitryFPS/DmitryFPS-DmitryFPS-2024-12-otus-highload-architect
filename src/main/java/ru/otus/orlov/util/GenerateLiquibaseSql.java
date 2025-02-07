package ru.otus.orlov.util;

import com.github.javafaker.Faker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GenerateLiquibaseSql {

    public static class User {
        final String firstName;
        final String lastName;
        final String birthDate;
        final String gender;
        final Set<String> interests;
        final String city;
        final String email;
        final String password;
        final String role;

        public User(final String firstName,
                    final String lastName,
                    final String birthDate,
                    final String gender,
                    final Set<String> interests,
                    final String city,
                    final String email,
                    final String password,
                    final String role) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
            this.gender = gender;
            this.interests = interests;
            this.city = city;
            this.email = email;
            this.password = password;
            this.role = role;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final User user = (User) o;
            return email.equals(user.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }
    }

    public static void main(String[] args) {
        final int numRecords = 1_000_000; // Количество записей
        final int recordsPerFile = 10_000; // Количество записей на файл
        final int numFiles = numRecords / recordsPerFile; // Количество файлов

        // Шаблоны для создания имен файлов
        final String sqlFilePathTemplate = "db/changelog/scheme/migration-%d.sql";
        final Path resourcesPath = Paths.get("src", "main", "resources").toAbsolutePath();

        final Faker faker = new Faker();
        final Set<User> users = new HashSet<>();
        final Set<String> emails = new HashSet<>();

        // Цикл для создания файлов
        for (int fileIndex = 1; fileIndex <= numFiles; fileIndex++) {
            // Формируем пути к файлам
            final Path sqlOutputPath = resourcesPath.resolve(String.format(sqlFilePathTemplate, fileIndex));

            // Создаем SQL-файл
            createSqlFile(sqlOutputPath.toFile(), faker, users, emails, fileIndex, recordsPerFile);
        }
    }

    private static void createSqlFile(File file, Faker faker, Set<User> users, Set<String> emails, int fileIndex, int recordsPerFile) {
        // Создаем директории и файл, если они не существуют
        if (!file.exists()) {
            try {
                final boolean mkdirs = file.getParentFile().mkdirs();
                System.out.println("Создан каталог: " + mkdirs);

                final boolean newFile = file.createNewFile();
                System.out.println("Создан SQL-файл: " + newFile);
            } catch (final IOException ignore) {
            }
        }

        // Записываем данные в SQL-файл
        try (BufferedWriter sqlWriter = new BufferedWriter(new FileWriter(file))) {
            // Начало транзакции для Liquibase
            sqlWriter.write("--liquibase formatted sql\n\n");

            // Основной changeset
            sqlWriter.write("-- Генерация данных для таблиц users, tokens и связей\n");
            sqlWriter.write("-- changeset orlov:migration-" + fileIndex + "\n");

            // Собираем данные для массовой вставки
            StringBuilder citiesInsert = new StringBuilder("INSERT INTO cities (name) VALUES ");
            StringBuilder usersInsert = new StringBuilder("INSERT INTO users (first_name, last_name, birth_date, gender, email, password, city_id) VALUES ");
            StringBuilder userRolesInsert = new StringBuilder("INSERT INTO user_roles (user_id, role_id) VALUES ");
            StringBuilder userInterestsInsert = new StringBuilder("INSERT INTO user_interests (user_id, interest_id) VALUES ");

            for (int i = 0; i < recordsPerFile; i++) {
                final int recordNumber = (fileIndex - 1) * recordsPerFile + i; // Общий номер записи
                final String firstName = faker.name().firstName();
                final String lastName = faker.name().lastName();
                final String birthDate = formatDate(faker.date().birthday(18, 65));
                final String gender = getRandomGender();
                final Set<String> interests = getRandomInterests();
                final String city = faker.address().city();
                final String email = getUniqueEmail(faker, emails);
                final String password = faker.internet().password(8, 10);
                final String role = getRandomRole();

                final User user = new User(firstName, lastName, birthDate, gender, interests, city, email, password, role);
                users.add(user);
                emails.add(email);

                // Добавляем данные для вставки в cities
                if (i > 0) citiesInsert.append(", ");
                citiesInsert.append(String.format("('%s')", escapeSql(city)));

                // Добавляем данные для вставки в users
                if (i > 0) usersInsert.append(", ");
                usersInsert.append(String.format(
                        "('%s', '%s', '%s', '%s'::gender_enum, '%s', '%s', (SELECT id FROM cities WHERE name = '%s' LIMIT 1))",
                        escapeSql(firstName),
                        escapeSql(lastName),
                        escapeSql(birthDate),
                        escapeSql(gender),
                        escapeSql(email),
                        escapeSql(password),
                        escapeSql(city)
                ));

                // Добавляем данные для вставки в user_roles
                if (i > 0) userRolesInsert.append(", ");
                userRolesInsert.append(String.format(
                        "((SELECT id FROM users WHERE email = '%s' LIMIT 1), (SELECT id FROM roles WHERE role = '%s' LIMIT 1))",
                        escapeSql(email),
                        escapeSql(role)
                ));

                // Добавляем данные для вставки в user_interests
                for (final String interest : interests) {
                    if (userInterestsInsert.length() > "INSERT INTO user_interests (user_id, interest_id) VALUES ".length()) {
                        userInterestsInsert.append(", ");
                    }
                    userInterestsInsert.append(String.format(
                            "((SELECT id FROM users WHERE email = '%s' LIMIT 1), (SELECT id FROM interests WHERE description = '%s' LIMIT 1))",
                            escapeSql(email),
                            escapeSql(interest)
                    ));
                }

                if (recordNumber % 1000 == 0) {
                    System.out.printf("Создана запись: %d\n", recordNumber);
                }
            }

            // Записываем массовые вставки в файл
            sqlWriter.write(citiesInsert.toString() + ";\n\n"); // Убрали ON CONFLICT
            sqlWriter.write(usersInsert.toString() + ";\n\n");
            sqlWriter.write(userRolesInsert.toString() + ";\n\n");
            sqlWriter.write(userInterestsInsert.toString() + ";\n\n");

            System.out.println("SQL-файл " + file.getName() + " успешно создан.");

        } catch (final IOException e) {
            System.err.println("Ошибка при записи в SQL файл: " + e.getMessage());
        }
    }

    private static String getUniqueEmail(final Faker faker,
                                         final Set<String> emails) {
        String email;
        do {
            email = faker.internet().emailAddress();
        } while (emails.contains(email));
        return email;
    }

    private static String escapeSql(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    private static String formatDate(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private static String getRandomGender() {
        return Math.random() < 0.5 ? "MALE" : "FEMALE";
    }

    private static Set<String> getRandomInterests() {
        final String[] interests = {"reading", "sports", "music", "traveling", "gaming", "cooking", "photography"};
        final int count = (int) (Math.random() * 3) + 1; // От 1 до 3 интересов
        final Set<String> result = new HashSet<>();
        for (int i = 0; i < count; i++) {
            result.add(interests[(int) (Math.random() * interests.length)]);
        }
        return result;
    }

    private static String getRandomRole() {
        return Math.random() < 0.9 ? "USER" : "ADMIN";
    }
}