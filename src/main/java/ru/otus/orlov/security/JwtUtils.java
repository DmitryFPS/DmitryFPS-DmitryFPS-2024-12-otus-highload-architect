package ru.otus.orlov.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/** Утилита для работы с JSON Web Token (JWT) */
@Component
public class JwtUtils {

    /** Одна тысяча */
    private static final int ONE_THOUSAND = 1000;

    /** Секретный ключ для подписи токенов */
    @Value("${jwt.secret}")
    private String secret;

    /** Срок действия токенов в миллисекундах */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Возвращает секретный ключ для подписи токенов
     *
     * @return секретный ключ
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует JWT для указанного пользователя
     *
     * @param userDetails пользователь
     * @return JWT
     */
    public String generateToken(final UserDetails userDetails) {
        final Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Создает JWT с указанными полями и субъектом
     *
     * @param claims  поля
     * @param subject субъект
     * @return JWT
     */
    private String createToken(final Map<String, Object> claims,
                               final String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * ONE_THOUSAND))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Проверяет валидность JWT для указанного пользователя
     *
     * @param token       JWT
     * @param userDetails пользователь
     * @return true, если JWT валидный, иначе false
     */
    public Boolean validateToken(final String token,
                                 final UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Извлекает имя пользователя из JWT
     *
     * @param token JWT
     * @return имя пользователя
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия из JWT
     *
     * @param token JWT
     * @return дата истечения срока действия
     */
    public Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает указанное поле из JWT
     *
     * @param token          JWT
     * @param claimsResolver функция для извлечения поля
     * @param <T>           тип извлекаемого поля
     * @return значение поля
     */
    private <T> T extractClaim(final String token,
                               final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все поля из JWT
     *
     * @param token JWT
     * @return все поля JWT
     */
    private Claims extractAllClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Проверяет, истек ли срок действия JWT
     *
     * @param token JWT
     * @return true, если JWT истек, иначе false
     */
    private Boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }
}
