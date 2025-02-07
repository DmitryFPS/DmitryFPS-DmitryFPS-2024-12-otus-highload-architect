package ru.otus.orlov.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Класс обрабатывает отказы в доступе и перенаправляет пользователей на страницу с запретом */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Обрабатывает исключения отказа в доступе.
     * Устанавливает статус ответа на 403 (Запрещено) и перенаправляет пользователя на URL /forbidden
     *
     * @param request               HTTP-запрос
     * @param response              HTTP-ответ
     * @param accessDeniedException исключение отказа в доступе
     * @throws IOException если возникает ошибка при перенаправлении
     */
    @Override
    public void handle(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.sendRedirect(request.getContextPath() + "/forbidden");
    }
}
