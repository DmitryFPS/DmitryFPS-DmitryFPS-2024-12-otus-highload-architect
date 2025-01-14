package ru.otus.orlov.repositories;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.orlov.entity.Interest;


/** Репозиторий для интересов */
public interface InterestRepository extends JpaRepository<Interest, Long> {
    /**
     * Находит интересы
     *
     * @param descriptions описание интересов
     * @return List<Interest>, содержащий объекты {@link Interest}, если они найдены
     */
    @Query("SELECT i FROM Interest i WHERE i.description IN :descriptions")
    List<Interest> findByDescriptions(@Param("descriptions") final Set<String> descriptions);
}
