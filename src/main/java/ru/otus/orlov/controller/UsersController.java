package ru.otus.orlov.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.orlov.dto.UserCreateDto;
import ru.otus.orlov.dto.UserDto;
import ru.otus.orlov.entity.User;
import ru.otus.orlov.services.UserService;


/**
 * Контроллер для работы с пользователями.
 * Предоставляет REST-эндпоинты для получения информации о пользователях и их регистрации.
 * Использует аннотацию {@link RestController} для обозначения класса как контроллера,
 * который возвращает данные в формате JSON.
 *
 * @see RestController
 * @see GetMapping
 * @see PostMapping
 * @see UserService
 */
@RestController
@RequiredArgsConstructor
public class UsersController {

    /**
     * Сервис для работы с пользователями.
     * Используется для выполнения бизнес-логики, связанной с пользователями.
     */
    private final UserService userService;


    /**
     * Возвращает информацию о пользователе по его ID
     *
     * @param id идентификатор пользователя, информацию о котором нужно получить
     * @return объект {@link UserDto}, содержащий информацию о пользователе
     * @see UserDto
     * @see UserService#findById(Long)
     * @see GetMapping
     * @see PathVariable
     */
    @GetMapping("/api/v1/user/{id}")
    public UserDto getUserById(@PathVariable("id") final Long id) {
        return userService.findById(id);
    }

    /**
     * Регистрирует нового пользователя
     *
     * @param userCreateDto объект {@link UserCreateDto}, содержащий данные для регистрации нового пользователя
     * @return объект {@link UserDto}, содержащий информацию о зарегистрированном пользователе
     * @see UserCreateDto
     * @see UserDto
     * @see UserService#create(UserCreateDto)
     * @see PostMapping
     * @see ResponseStatus
     * @see HttpStatus#CREATED
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/user/register")
    public UserDto getUserRegister(@RequestBody final UserCreateDto userCreateDto) {
        return userService.create(userCreateDto);
    }

    /**
     * Обрабатывает GET-запрос для поиска пользователей по префиксу имени и фамилии.
     * Принимает параметры запроса "first_name" и "last_name" и возвращает список пользователей,
     * чьи имена и фамилии начинаются с указанных префиксов.
     * Результаты сортируются по идентификатору пользователя (id).
     *
     * @param firstName префикс имени для поиска (например, "Конст" для "Константин").
     * @param lastName  префикс фамилии для поиска (например, "Оси" для "Осипов").
     * @return ResponseEntity со списком пользователей и статусом HTTP 200 (OK).
     */
    @GetMapping(value = "/api/v1/user/search", params = {"first_name", "last_name"})
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam("first_name") final String firstName,
            @RequestParam("last_name") final String lastName) {

        final List<User> users = userService.searchUsersByFirstNameAndLastName(firstName, lastName);
        return ResponseEntity.ok(users);
    }
}
