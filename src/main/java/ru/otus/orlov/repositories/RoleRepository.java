package ru.otus.orlov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.orlov.entity.Role;

import java.util.Set;

/** Репозиторий ролей */
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Ищет роли по названию с использованием HQL.
     *
     * @param roles Название ролей
     * @return Set<Role>, содержит роли, если найдены
     */
    @Query("SELECT r FROM Role r WHERE r.role IN :roles")
    Set<Role> findByRoles(@Param("roles") final Set<String> roles);
}
