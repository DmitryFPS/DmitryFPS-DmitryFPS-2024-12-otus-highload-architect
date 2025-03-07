package ru.otus.orlov.util;

import com.github.javafaker.Faker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GenerateCSV {

    private static final int NUM_RECORDS = 1_000_000;
    private static final int NUM_CITIES = 5;
    private static final int NUM_INTERESTS = 7;
    private static final int NUM_ROLES = 2;
    private static final int BUFFER_SIZE = 8192 * 32; // Увеличил размер буфера

    public static void main(String[] args) {
        final String resourcesPath = "src/main/resources/migration/"; // Путь к директории resources
        final long startTime = System.currentTimeMillis();

        System.out.println("Начало генерации данных...");

        generateUsersCSV(resourcesPath + "users.csv");
        generateUserRolesCSV(resourcesPath + "user_roles.csv");
        generateUserInterestsCSV(resourcesPath + "user_interests.csv");
        generatePostsCSV(resourcesPath + "posts.csv");
        generateUserFriendsCSV(resourcesPath + "user_friends.csv");

        final long endTime = System.currentTimeMillis();
        System.out.println("Генерация данных завершена.");
        System.out.println("Общее время выполнения: " + (endTime - startTime) / 1000 + " секунд");
    }

    private static void generateUsersCSV(final String filename) {
        System.out.println("Генерация users.csv...");
        final File file = new File(filename);
        createDirectoryIfNotExists(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), BUFFER_SIZE)) {
            final Faker faker = new Faker();
            final Random random = new Random();
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            final Set<String> emails = new HashSet<>(); // Для проверки уникальности email

            // Генерация списка имен и фамилий заранее
            final List<String> firstNames = generateNames(faker, 1000, true);
            final List<String> lastNames = generateNames(faker, 1000, false);

            // Генерация данных в одном потоке
            for (int i = 0; i <= NUM_RECORDS; i++) {
                final String firstName = firstNames.get(random.nextInt(firstNames.size()));
                final String lastName = lastNames.get(random.nextInt(lastNames.size()));
                final LocalDate birthDate = LocalDate.now().minusYears(random.nextInt(50) + 18);
                final String gender = random.nextBoolean() ? "MALE" : "FEMALE";
                final String email = generateEmail(firstName, lastName, faker, i); // Уникальный email
                final String password = "$2a$12$..q/lnF3X4.tNCTt/cBsJexVEV2KfKzlYlB78O35YagkD076EZ7JO"; // Пароль с хэшированием root
                final int cityId = random.nextInt(NUM_CITIES) + 1;

                // Проверяю уникальность email
                if (emails.contains(email)) {
                    throw new IllegalStateException("Дубликат email: " + email);
                }
                emails.add(email);

                final String record = String.format("%s,%s,%s,%s,%s,%s,%b,%d%n",
                        firstName, lastName, birthDate.format(formatter), gender, email, password, true, cityId);

                writer.write(record);

                logProgress(i, "users.csv", writer);
            }

            writer.flush(); // Принудительно сбрасываю буфер перед закрытием
            System.out.println("users.csv успешно создан.");
        } catch (final IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static void generateUserRolesCSV(final String filename) {
        System.out.println("Генерация user_roles.csv...");
        final File file = new File(filename);
        createDirectoryIfNotExists(file);

        final Set<String> uniqueRoles = new HashSet<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), BUFFER_SIZE)) {
            final Random random = new Random();

            for (int i = 1; i <= NUM_RECORDS; i++) {
                final Set<Integer> roles = generateUniqueRandomNumbers(random, 1, 2, NUM_ROLES);

                for (final int roleId : roles) {
                    uniqueRoles.add(String.format("%d,%d%n", i, roleId));
                }

                logProgress(i, "user_roles.csv", writer);
            }

            for (final String role : uniqueRoles) {
                writer.write(role);
            }

            writer.flush(); // Принудительно сбрасываю буфер перед закрытием
            uniqueRoles.clear();
            System.out.println("user_roles.csv успешно создан.");
        } catch (final IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static void generateUserInterestsCSV(final String filename) {
        System.out.println("Генерация user_interests.csv...");
        final File file = new File(filename);
        createDirectoryIfNotExists(file);

        final Set<String> uniqueInterest = new HashSet<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), BUFFER_SIZE)) {
            final Random random = new Random();

            for (int i = 1; i <= NUM_RECORDS; i++) {
                final Set<Integer> interests = generateUniqueRandomNumbers(random, 3, 7, NUM_INTERESTS);
                for (final int interestId : interests) {
                    String record = String.format("%d,%d%n", i, interestId);
                    uniqueInterest.add(record);
                }
                logProgress(i, "user_interests.csv", writer);
            }

            for (final String interest : uniqueInterest) {
                writer.write(interest);
            }

            uniqueInterest.clear();
            writer.flush(); // Принудительно сбрасываю буфер перед закрытием
            System.out.println("user_interests.csv успешно создан.");
        } catch (final IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static void generatePostsCSV(final String filename) {
        System.out.println("Генерация posts.csv...");
        final File file = new File(filename);
        createDirectoryIfNotExists(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), BUFFER_SIZE)) {
            final Faker faker = new Faker();
            final Random random = new Random();
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            int totalPosts = 0; // Счетчик общего количества постов

            for (int userId = 1; userId <= NUM_RECORDS; userId++) {
                int numPosts = random.nextInt(6) + 5; // От 5 до 10 постов

                for (int i = 0; i < numPosts; i++) {
                    final String content = faker.lorem().sentence(random.nextInt(10) + 1); // Длина поста до 10 символов
                    final LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(365)); // Дата создания в пределах года
                    final String formattedCreatedAt = createdAt.format(formatter);

                    final String record = String.format("%s,%s,%d%n",
                            content, formattedCreatedAt, userId);

                    writer.write(record);
                    totalPosts++;
                }

                // Логирование прогресса
                if (userId % 10_000 == 0) {
                    System.out.printf("Обработано пользователей: %d, сгенерировано постов: %d%n", userId, totalPosts);
                    writer.flush(); // Принудительно сбрасываем буфер
                }
            }

            writer.flush(); // Принудительно сбрасываем буфер перед закрытием
            System.out.println("posts.csv успешно создан. Всего постов: " + totalPosts);
        } catch (final IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static void generateUserFriendsCSV(final String filename) {
        System.out.println("Генерация user_friends.csv...");
        final File file = new File(filename);
        createDirectoryIfNotExists(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), BUFFER_SIZE)) {
            final Random random = new Random();

            // Создаем список всех возможных ID пользователей
            final List<Integer> allUserIds = new ArrayList<>();
            for (int i = 1; i <= NUM_RECORDS; i++) {
                allUserIds.add(i);
            }

            // Перемешиваем список всех ID один раз
            Collections.shuffle(allUserIds, random);

            int totalFriendships = 0; // Счетчик общего количества дружеских связей

            // Используем StringBuilder для накопления строк
            final StringBuilder buffer = new StringBuilder();

            for (int userId = 1; userId <= NUM_RECORDS; userId++) {
                int numFriends = random.nextInt(301) + 200; // От 200 до 500 друзей

                // Выбираем друзей из перемешанного списка, исключая текущего пользователя
                int friendIndex = 0;
                int friendsAdded = 0;

                while (friendsAdded < numFriends) {
                    int friendId = allUserIds.get(friendIndex);

                    // Исключаем текущего пользователя
                    if (friendId != userId) {
                        buffer.append(String.format("%d,%d%n", userId, friendId));
                        totalFriendships++;
                        friendsAdded++;
                    }

                    friendIndex++;

                    // Если дошли до конца списка, начинаем с начала
                    if (friendIndex >= allUserIds.size()) {
                        friendIndex = 0;
                    }
                }

                // Логирование прогресса и запись в файл
                if (userId % 10_000 == 0) {
                    writer.write(buffer.toString()); // Записываем накопленные данные
                    buffer.setLength(0); // Очищаем буфер
                    System.out.printf("Обработано пользователей: %d, сгенерировано дружеских связей: %d%n", userId, totalFriendships);
                    writer.flush(); // Принудительно сбрасываем буфер
                }
            }

            // Записываем оставшиеся данные
            if (buffer.length() > 0) {
                writer.write(buffer.toString());
            }

            writer.flush(); // Принудительно сбрасываем буфер перед закрытием
            System.out.println("user_friends.csv успешно создан. Всего дружеских связей: " + totalFriendships);
        } catch (final IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static void createDirectoryIfNotExists(final File file) {
        final boolean mkdirs = file.getParentFile().mkdirs();
        System.out.printf("Создаем директорию %s%n", mkdirs);
    }

    private static List<String> generateNames(final Faker faker,
                                              final int count,
                                              final boolean isFirstName) {
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            names.add(isFirstName ? faker.name().firstName() : faker.name().lastName());
        }
        return names;
    }

    private static String generateEmail(final String firstName,
                                        final String lastName,
                                        final Faker faker,
                                        final int userId) {
        return String.format("%s.%s%d@%s",
                firstName.toLowerCase(),
                lastName.toLowerCase(),
                userId, // Использую userId для уникальности
                faker.internet().domainName());
    }

    private static Set<Integer> generateUniqueRandomNumbers(final Random random,
                                                            final int minCount,
                                                            final int maxCount,
                                                            final int maxValue) {
        final int count = random.nextInt(maxCount - minCount + 1) + minCount;
        final Set<Integer> numbers = new HashSet<>();
        while (numbers.size() < count) {
            numbers.add(random.nextInt(maxValue) + 1);
        }
        return numbers;
    }

    private static void logProgress(final int currentRecord,
                                    final String fileName,
                                    final BufferedWriter writer) throws IOException {
        if (currentRecord % 100_000 == 0) {
            System.out.println("Сгенерировано записей для " + fileName + ": " + currentRecord);
            writer.flush(); // Принудительно сбрасываю буфер
        }
    }
}
