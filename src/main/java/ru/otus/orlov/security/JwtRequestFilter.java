package ru.otus.orlov.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.otus.orlov.constants.TextConstants;
import ru.otus.orlov.entity.Token;
import ru.otus.orlov.repositories.UserRepository;

/** Фильтр для обработки запросов, содержащих JWT */
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    /** Заголовок авторизации */
    private static final String AUTHORIZATION = "Authorization";

    /** Префикс токена */
    private static final String BEARER = "Bearer";

    /** Сервис для работы с пользователями */
    private final UserDetailsServiceImpl userDetailsService;

    /** Утилиты для работы с JWT */
    private final JwtUtils jwtUtils;

    /** Репозиторий для работы с данными пользователя */
    private final UserRepository userRepository;


    /**
     * Обрабатывает каждый запрос
     *
     * @param request  запрос
     * @param response ответ
     * @param chain    цепь фильтров
     */
    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain chain) throws ServletException, IOException {
        final String jwt = extractToken(request);
        if (jwt == null) {
            chain.doFilter(request, response);
            return;
        }
        final String email = jwtUtils.extractUsername(jwt);
        if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!isTokenValid(jwt, userDetails)) {
            if (isRefreshTokenValid(email)) {
                final String newAccessToken = jwtUtils.generateToken(userDetails);
                response.setHeader("New-Access-Token", newAccessToken);
            } else {
                throw new RuntimeException("Access Token истек, а Refresh Token недействителен");
            }
        }
        setAuthentication(userDetails, request);
        chain.doFilter(request, response);
    }

    /** Извлекаем токен из ответа */
    private String extractToken(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER + TextConstants.EMPTY_STRING)) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /** Валиден ли токен */
    private boolean isTokenValid(final String jwt, final UserDetails userDetails) {
        final Token token = userRepository.findTokenByUserEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с email %s не найден".formatted(userDetails.getUsername())));

        return token.getAccessTokenExpiration() != null
                && !token.getAccessTokenExpiration().isBefore(LocalDateTime.now())
                && jwtUtils.validateToken(jwt, userDetails);
    }

    /** Валиден ли Рефреш Токен */
    private boolean isRefreshTokenValid(final String email) {
        final Token token = userRepository.findTokenByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email %s не найден".formatted(email)));

        return token.getRefreshToken() != null
                && token.getRefreshTokenExpiration() != null
                && !token.getRefreshTokenExpiration().isBefore(LocalDateTime.now());
    }

    /** Устанавливает аутентификацию в SecurityContext */
    private void setAuthentication(final UserDetails userDetails,
                                   final HttpServletRequest request) {
        final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
