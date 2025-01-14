package ru.otus.orlov.dto;

import java.util.Date;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс, представляющий DTO (Data Transfer Object) для создания нового пользователя.
 * Используется для передачи данных, необходимых для регистрации пользователя.
 */
@Setter
@Getter
public class UserCreateDto {

    /** Имя пользователя */
    private String firstName;

    /** Фамилия пользователя */
    private String lastName;

    /** Дата рождения пользователя */
    private Date birthDate;

    /**
     * Пол пользователя.
     * Ожидаемые значения: "MALE", "FEMALE" или другие, в зависимости от реализации.
     */
    private String gender;

    /**
     * Интересы пользователя.
     * Может содержать текстовое описание увлечений или хобби пользователя.
     */
    private Set<String> interests;

    /** Город, в котором проживает пользователь */
    private String city;

    /**
     * Электронная почта пользователя.
     * Используется для идентификации пользователя и отправки уведомлений.
     */
    private String email;

    /**
     * Пароль пользователя.
     * Должен быть закодирован перед сохранением в базу данных.
     */
    private String password;

    /**
     * Роль пользователя в системе.
     * Ожидаемые значения: "USER", "ADMIN" или другие, в зависимости от реализации.
     */
    private Set<String> role;
}
