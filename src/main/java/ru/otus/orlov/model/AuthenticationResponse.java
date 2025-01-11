package ru.otus.orlov.model;

/** Ответ на запрос аутентификации, содержащий JWT-токен */
public record AuthenticationResponse(String jwt) {
    // Без тела
}
