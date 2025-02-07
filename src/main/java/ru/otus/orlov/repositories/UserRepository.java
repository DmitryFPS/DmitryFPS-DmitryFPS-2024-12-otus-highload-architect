package ru.otus.orlov.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import ru.otus.orlov.entity.User;

/** Репозиторий для работы с данными пользователя */
public interface UserRepository extends JpaRepository<User, Long> {
    /** Поиск пользователя по email с использованием HQL */
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "city-roles-interests-token-entity-graph")
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") final String email);

    /** Поиск пользователя по id */
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "city-roles-interests-token-entity-graph")
    @NonNull
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findById(@Param("id") @NonNull final Long id);
}
