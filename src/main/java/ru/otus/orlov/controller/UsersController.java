package ru.otus.orlov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.orlov.dto.UserCreateDto;
import ru.otus.orlov.dto.UserDto;
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
}
