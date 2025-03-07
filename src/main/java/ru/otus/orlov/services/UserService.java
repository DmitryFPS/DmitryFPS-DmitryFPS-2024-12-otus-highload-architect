package ru.otus.orlov.services;

import java.util.List;
import ru.otus.orlov.dto.UserCreateDto;
import ru.otus.orlov.dto.UserDto;
import ru.otus.orlov.entity.User;

/**
 * Сервис для работы с пользователями.
 * Этот интерфейс предоставляет методы для поиска пользователей по идентификатору
 * и создания новых пользователей.
 */
public interface UserService {
    /**
     * Находит пользователя по его идентификатору.
     * Возвращает данные пользователя в виде объекта {@link UserDto}.
     * Если пользователь с указанным идентификатором не найден, может быть выброшено исключение.
     *
     * @param id идентификатор пользователя
     * @return объект {@link UserDto}, содержащий данные пользователя
     * @throws jakarta.persistence.EntityNotFoundException если пользователь с указанным идентификатором не найден
     */
    UserDto findById(final Long id);

    /**
     * Создает нового пользователя на основе предоставленных данных.
     * Принимает объект {@link UserCreateDto}, содержащий данные для создания пользователя,
     * и возвращает объект {@link UserDto} с информацией о созданном пользователе.
     *
     * @param userCreateDto объект {@link UserCreateDto}, содержащий данные для создания пользователя
     * @return объект {@link UserDto}, содержащий данные созданного пользователя
     */
    UserDto create(final UserCreateDto userCreateDto);

    /**
     * Ищет пользователей по префиксу имени и фамилии.
     * Делегирует вызов репозиторию для выполнения поиска в базе данных.
     *
     * @return список пользователей, удовлетворяющих условиям поиска.
     */
    List<User> searchUsersByFirstNameAndLastName(final String firstName, final String lastName);
}
