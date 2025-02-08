package ru.otus.orlov.repositories;

import java.util.List;
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

    /**
     * Ищет пользователей по префиксу имени и фамилии.
     * Имя и фамилия должны начинаться с указанных префиксов.
     * Результаты сортируются по идентификатору пользователя (id).
     *
     * @param firstName префикс имени для поиска (например, "Конст" для "Константин").
     * @param lastName  префикс фамилии для поиска (например, "Оси" для "Осипов").
     * @return список пользователей, удовлетворяющих условиям поиска.
     */
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "city-roles-interests-token-entity-graph")
    @Query("SELECT u FROM User u WHERE u.firstName LIKE :firstName% AND u.lastName LIKE :lastName% ORDER BY u.id")
    List<User> findByFirstNameAndLastName(@Param("firstName") final String firstName,
                                          @Param("lastName") final String lastName);
}
