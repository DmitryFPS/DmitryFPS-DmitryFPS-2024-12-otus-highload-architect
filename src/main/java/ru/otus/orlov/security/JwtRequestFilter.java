package ru.otus.orlov.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static ru.otus.orlov.constants.TextConstants.EMPTY_STRING;

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


    /**
     * Обрабатывает каждый запрос
     *
     * @param request  запрос
     * @param response ответ
     * @param chain    цепь фильтров
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain chain) throws ServletException, IOException {
        // Получение заголовка авторизации из запроса
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        String username = null;
        String jwt = null;
        // Проверка, что заголовок авторизации не пустой и начинается с префикса токена. извлекаем JWT из заголовка
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER + EMPTY_STRING)) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtils.extractUsername(jwt);
        }
        // Если имя пользователя не пустое и контекст безопасности не содержит аутентификацию,
        // загружаем пользователя из базы данных по имени пользователя
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // Если JWT валидный, создаем токен аутентификации пользователя и устанавливаем его в контекст безопасности
            if (jwtUtils.validateToken(jwt, userDetails)) {
                final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        // Продолжаем выполнение цепи фильтров
        chain.doFilter(request, response);
    }
}
