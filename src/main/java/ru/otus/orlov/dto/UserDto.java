package ru.otus.orlov.dto;

import java.util.Date;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import ru.otus.orlov.customers.Gender;

/**
 * Класс, представляющий DTO (Data Transfer Object) для передачи данных о пользователе.
 * Используется для обмена информацией о пользователе между слоями приложения.
 */
@Getter
@Setter
public class UserDto {

    /** Уникальный идентификатор пользователя */
    private Long id;

    /** Имя пользователя */
    private String firstName;

    /** Фамилия пользователя */
    private String lastName;

    /** Дата рождения пользователя */
    private Date birthDate;

    /**
     * Пол пользователя.
     * Ожидаемые значения: {@link Gender#MALE}, {@link Gender#FEMALE} или другие, в зависимости от реализации.
     */
    private Gender gender;

    /**
     * Интересы или хобби пользователя.
     * Может содержать текстовое описание увлечений пользователя.
     */
    private Set<String> interests;

    /** Город, в котором проживает пользователь */
    private String city;

    /**
     * Электронная почта пользователя.
     * Используется для идентификации пользователя и отправки уведомлений.
     */
    private String email;

    /** Статус активности пользователя */
    private Boolean isActive;
}
