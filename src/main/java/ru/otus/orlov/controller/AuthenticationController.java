package ru.otus.orlov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.orlov.model.AuthenticationRequest;
import ru.otus.orlov.model.AuthenticationResponse;
import ru.otus.orlov.security.JwtUtils;
import ru.otus.orlov.security.UserDetailsServiceImpl;


/** Контроллер для аутентификации пользователей */
@RequiredArgsConstructor
@RestController
public class AuthenticationController {

    /** Менеджер аутентификации */
    private final AuthenticationManager authenticationManager;

    /** Сервис для получения деталей пользователя */
    private final UserDetailsServiceImpl userDetailsService;

    /** Утилита для генерации JWT-токенов */
    private final JwtUtils jwtUtils;


    /**
     * Создает JWT-токен для аутентифицированного пользователя
     *
     * @param authenticationRequest запрос на аутентификацию
     * @return ответ с JWT-токеном
     */
    @PostMapping("/api/v1/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody final AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(), authenticationRequest.getPassword()
                )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
