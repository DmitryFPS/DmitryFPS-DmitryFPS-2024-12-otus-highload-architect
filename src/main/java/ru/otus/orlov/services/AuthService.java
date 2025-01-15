package ru.otus.orlov.services;

import ru.otus.orlov.model.AuthResponse;
import ru.otus.orlov.model.LogoutRequest;


/**
 * Сервис для работы с аутентификацией и управлением токенами.
 * Предоставляет методы для аутентификации пользователей, обновления токенов и выхода из системы.
 */
public interface AuthService {
    /**
     * Выполняет аутентификацию пользователя на основе электронной почты и пароля.
     *
     * @param email    электронная почта пользователя.
     * @param password пароль пользователя.
     * @return объект {@link AuthResponse}, содержащий access-токен и refresh-токен.
     */
    AuthResponse authenticate(final String email, final String password);

    /**
     * Обновляет access-токен на основе предоставленного refresh-токена.
     *
     * @param refreshToken refresh-токен, используемый для получения нового access-токена.
     * @return новый access-токен.
     */
    String refreshToken(final String refreshToken);

    /**
     * Выполняет выход пользователя из системы, аннулируя его токены.
     *
     * @param logoutRequest запрос, содержащий информацию, необходимую для выхода из системы.
     */
    void logout(final LogoutRequest logoutRequest);
}
