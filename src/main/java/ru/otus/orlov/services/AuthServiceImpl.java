package ru.otus.orlov.services;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.orlov.entity.Token;
import ru.otus.orlov.entity.User;
import ru.otus.orlov.model.AuthResponse;
import ru.otus.orlov.model.LogoutRequest;
import ru.otus.orlov.repositories.UserRepository;
import ru.otus.orlov.security.JwtUtils;
import ru.otus.orlov.security.UserDetailsServiceImpl;
import ru.otus.orlov.util.DateUtil;

import static java.util.Optional.ofNullable;

/**
 * Сервис для работы с аутентификацией и управлением токенами.
 * Предоставляет методы для аутентификации пользователей, обновления токенов и выхода из системы.
 */
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * Утилита для работы с JWT (JSON Web Token).
     * Используется для создания, проверки и обработки токенов.
     */
    private final JwtUtils jwtUtils;

    /**
     * Репозиторий для работы с данными пользователей.
     * Предоставляет методы для доступа к данным пользователей в базе данных.
     */
    private final UserRepository userRepository;

    /** Сервис Токена */
    private final TokenService tokenService;

    /**
     * Сервис для загрузки данных пользователя.
     * Реализует интерфейс UserDetailsService и используется для аутентификации пользователей.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Кодировщик паролей.
     * Используется для шифрования и проверки паролей пользователей.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Выполняет аутентификацию пользователя на основе электронной почты и пароля.
     *
     * @param email    электронная почта пользователя.
     * @param password пароль пользователя.
     * @return объект {@link AuthResponse}, содержащий access-токен и refresh-токен.
     */
    @Transactional
    @Override
    public AuthResponse authenticate(final String email, final String password) {
        // Проверка логина и пароля
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Неверный email или password");
        }
        // Генерация Access Token и Refresh Token
        final String accessToken = jwtUtils.generateToken(userDetails);
        final String refreshToken = jwtUtils.generateRefreshToken(userDetails);
        // Время протухания токена
        final Date accessTokenExpiration = jwtUtils.extractExpiration(accessToken);
        // Время протухания рефреш токена
        final Date refreshTokenExpiration = jwtUtils.extractExpiration(refreshToken);
        // Сохранение Refresh Token в БД
        final User user = userRepository.findWithTokenByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Пользователь с email %s не найден".formatted(email)));
        final Token token = ofNullable(user.getToken()).orElseGet(Token::new);
        token.setRefreshToken(refreshToken);
        token.setAccessTokenExpiration(DateUtil.asLocalDateTime(accessTokenExpiration));
        token.setRefreshTokenExpiration(DateUtil.asLocalDateTime(refreshTokenExpiration));
        user.setToken(token);
        userRepository.save(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Обновляет access-токен на основе предоставленного refresh-токена.
     *
     * @param refreshToken refresh-токен, используемый для получения нового access-токена.
     * @return новый access-токен.
     */
    @Transactional
    @Override
    public String refreshToken(final String refreshToken) {
        // Извлечение email из Refresh Token
        final String email = jwtUtils.extractUsername(refreshToken);

        // Поиск пользователя в БД
        final User user = userRepository.findWithTokenByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email %s не найден".formatted(email)));
        final Token token = user.getToken();
        // Проверка, что Refresh Token валиден и совпадает с сохраненным в БД
        if (jwtUtils.validateToken(refreshToken, user) && refreshToken.equals(token.getRefreshToken())) {
            // Генерация нового Access Token
            final String newToken = jwtUtils.generateToken(user);
            // Время протухания Access Token
            final Date accessTokenExpiration = jwtUtils.extractExpiration(newToken);
            // Сохраняем новое время протухания Access Token
            token.setAccessTokenExpiration(DateUtil.asLocalDateTime(accessTokenExpiration));
            user.setToken(token);
            userRepository.save(user);
            return newToken;
        } else {
            throw new RuntimeException("Неверный refresh token");
        }
    }

    /**
     * Выполняет выход пользователя из системы, аннулируя его токены.
     *
     * @param logoutRequest запрос, содержащий информацию, необходимую для выхода из системы.
     */
    @Transactional
    @Override
    public void logout(final LogoutRequest logoutRequest) {
        final String email = logoutRequest.getEmail();
        final User user = userRepository.findWithTokenByEmail(logoutRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email %s не найден".formatted(email)));
        final Token token = user.getToken();
        // Удаляем токен из БД
        tokenService.deleteToken(token);
        // Обнуляем токен у Сущности
        user.setToken(null);
        // Сохраняем изменения
        userRepository.save(user);
    }
}
