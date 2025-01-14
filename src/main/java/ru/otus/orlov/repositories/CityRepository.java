package ru.otus.orlov.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.orlov.entity.City;

/**
 * Репозиторий для работы с сущностями {@link City}.
 * Предоставляет методы для выполнения операций с городами в базе данных.
 * Расширяет {@link JpaRepository}, что позволяет использовать стандартные методы CRUD,
 * а также добавляет пользовательские методы для поиска городов.
 *
 * @see City
 * @see JpaRepository
 */
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Находит город по его названию.
     * Использует HQL-запрос для поиска города по точному совпадению названия.
     *
     * @param name название города для поиска.
     * @return {@link Optional}, содержащий найденный город, или {@link Optional#empty()}, если город не найден.
     */
    @Query("SELECT c FROM City c WHERE c.name = :name")
    Optional<City> findByName(@Param("name") String name);
}
