package ru.otus.orlov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.orlov.model.AuthResponse;
import ru.otus.orlov.model.AuthenticationRequest;
import ru.otus.orlov.model.LogoutRequest;
import ru.otus.orlov.model.RefreshTokenRequest;
import ru.otus.orlov.services.AuthService;


/** Контроллер для аутентификации пользователей */
@RequiredArgsConstructor
@RestController
public class AuthenticationController {
    /**
     * Сервис для работы с аутентификацией и управления токенами.
     * Используется для выполнения операций, таких как обновление токенов и выход из системы.
     */
    private final AuthService authService;


    /**
     * Создает JWT-токен для аутентифицированного пользователя
     *
     * @param authenticationRequest запрос на аутентификацию
     * @return ответ с JWT-токеном
     */
    @PostMapping("/api/v1/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody final AuthenticationRequest authenticationRequest) {
        // Используем AuthService для аутентификации
        final AuthResponse authResponse = authService.authenticate(
                authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Обновляет access-токен на основе предоставленного refresh-токена
     *
     * @param refreshTokenRequest запрос, содержащий refresh-токен
     * @return ответ с новым access-токеном и текущим refresh-токеном
     */
    @PostMapping("/api/v1/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        final String newAccessToken = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshTokenRequest.getRefreshToken()));
    }

    /**
     * Выполняет выход пользователя из системы, аннулируя его токены
     *
     * @param logoutRequest запрос, содержащий информацию, необходимую для выхода из системы
     * @return ответ с пустым телом и статусом 200 OK
     */
    @PostMapping("/api/v1/logout")
    public ResponseEntity<Void> logout(@RequestBody final LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok().build();
    }
}
