package ru.otus.orlov.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.orlov.entity.Token;
import ru.otus.orlov.repositories.TokenRepository;


/** Сервис Токена */
@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {
    /** Репозиторий Токена */
    private final TokenRepository tokenRepository;

    /** Удалить токен */
    @Override
    public void deleteToken(final Token token) {
        tokenRepository.delete(token);
    }
}
