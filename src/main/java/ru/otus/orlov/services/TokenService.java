package ru.otus.orlov.services;

import ru.otus.orlov.entity.Token;

/** Сервис Токена */
public interface TokenService {
    /** Удалить токен */
    void deleteToken(final Token token);
}
