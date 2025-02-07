package ru.otus.orlov.model;

import lombok.Data;


/** Запрос на аутентификацию, содержащий имя пользователя и пароль */
@Data
public class AuthenticationRequest {

    /** Наименование юзера */
    private String username;

    /** Пароль пользователя */
    private String password;
}
