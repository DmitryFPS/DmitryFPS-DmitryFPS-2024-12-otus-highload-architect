package ru.otus.orlov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.orlov.entity.Token;

/** Репозиторий Токена */
public interface TokenRepository extends JpaRepository<Token, Long> {
}
