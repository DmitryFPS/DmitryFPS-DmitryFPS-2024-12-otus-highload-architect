package ru.otus.orlov.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@SpringBootTest(classes = {JwtUtils.class})
class JwtUtilsTest {

    private static final String SECRET = "jCggmykJqBfJ3gGWUpyx95OsUAAMOPVaGXtrC4lQ81I=";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр JwtUtils
        jwtUtils = new JwtUtils();

        // Устанавливаем значения полей через ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400L);
    }

    @Test
    @WithMockUser(username = "ddd12@mail.ru", roles = {"ADMIN"})
    void testValidToken() {
        final UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        final String token = jwtUtils.generateToken(userDetails);

        try {
            // Преобразование секретного ключа
            final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

            // Проверка токена
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Вывод данных из токена
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("Issued At: " + new Date(claims.getIssuedAt().getTime()));
            System.out.println("Expiration: " + new Date(claims.getExpiration().getTime()));

            // Проверка срока действия
            if (claims.getExpiration().before(new Date())) {
                System.out.println("Токен истек.");
            } else {
                System.out.println("Токен действителен.");
            }
        } catch (Exception e) {
            System.out.println("Токен невалиден: " + e.getMessage());
        }
    }
}
