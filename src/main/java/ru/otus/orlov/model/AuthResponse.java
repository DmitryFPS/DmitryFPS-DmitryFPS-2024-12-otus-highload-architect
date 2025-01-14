package ru.otus.orlov.model;

import lombok.Data;

/**
 * Класс, представляющий ответ с токенами доступа и обновления.
 * Используется для передачи access-токена и refresh-токена клиенту
 * после успешной аутентификации или обновления токенов.
 */
@Data
public class AuthResponse {
    /** Access-токен, используемый для доступа к защищенным ресурсам */
    private String accessToken;

    /** Refresh-токен, используемый для получения нового access-токена по истечении срока его действия */
    private String refreshToken;

    /**
     * Конструктор для создания объекта ответа с токенами.
     *
     * @param accessToken  access-токен, который будет передан клиенту.
     * @param refreshToken refresh-токен, который будет передан клиенту.
     */
    public AuthResponse(final String accessToken,
                        final String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
