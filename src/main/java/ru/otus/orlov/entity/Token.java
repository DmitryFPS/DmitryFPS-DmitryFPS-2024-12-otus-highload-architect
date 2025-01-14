package ru.otus.orlov.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Сущность для токенов */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {
    /** Идентификатор пользователя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Рефреш токен */
    @Column(name = "refresh_token")
    private String refreshToken;

    /** Срок действия Access Token */
    @Column(name = "access_token_expiration")
    private LocalDateTime accessTokenExpiration;

    /** Срок действия Refresh Token */
    @Column(name = "refresh_token_expiration")
    private LocalDateTime refreshTokenExpiration;
}
