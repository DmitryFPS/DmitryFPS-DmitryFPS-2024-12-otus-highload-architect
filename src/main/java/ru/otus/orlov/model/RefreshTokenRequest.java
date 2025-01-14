package ru.otus.orlov.model;

import lombok.Data;

/**
 * Класс, представляющий запрос на обновление access-токена.
 * Используется для передачи refresh-токена, необходимого для получения нового access-токена.
 */
@Data
public class RefreshTokenRequest {
    /** Refresh-токен, который используется для получения нового access-токена */
    private String refreshToken;
}
