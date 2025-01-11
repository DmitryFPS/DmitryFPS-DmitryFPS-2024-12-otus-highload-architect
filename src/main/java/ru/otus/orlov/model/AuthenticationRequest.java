package ru.otus.orlov.model;

import lombok.Getter;
import lombok.Setter;


/** Запрос на аутентификацию, содержащий имя пользователя и пароль */
@Getter
@Setter
public class AuthenticationRequest {

    /** Наименование юзера */
    private String username;

    /** Пароль пользователя */
    private String password;
}
