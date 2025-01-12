package ru.otus.orlov.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.otus.orlov.dto.UserDto;
import ru.otus.orlov.entity.Interest;
import ru.otus.orlov.entity.User;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/** Маппер для Юзера */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    /**
     * Преобразует сущность User в UserDto.
     *
     * @param user сущность User
     * @return объект UserDto
     */
    @Mapping(target = "city", source = "city.name")
    @Mapping(target = "interests", source = "interests", qualifiedByName = "mapInterests")
    UserDto toDto(final User user);

    /**
     * Преобразует набор интересов (Set<Interest>) в набор строк (Set<String>).
     *
     * @param interests набор интересов
     * @return набор строк с описанием интересов
     */
    @Named("mapInterests")
    default Set<String> mapInterests(final Set<Interest> interests) {
        if (interests == null) {
            return Collections.emptySet();
        }
        return interests.stream()
                .map(Interest::getDescription)
                .collect(Collectors.toSet());
    }
}
