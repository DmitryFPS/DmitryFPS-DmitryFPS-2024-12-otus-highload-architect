package ru.otus.orlov.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.orlov.customers.Gender;
import ru.otus.orlov.dto.UserCreateDto;
import ru.otus.orlov.dto.UserDto;
import ru.otus.orlov.entity.City;
import ru.otus.orlov.entity.Interest;
import ru.otus.orlov.entity.Role;
import ru.otus.orlov.entity.User;
import ru.otus.orlov.exceptions.NotFoundException;
import ru.otus.orlov.exceptions.RoleNotFoundException;
import ru.otus.orlov.mapper.UserMapper;
import ru.otus.orlov.repositories.CityRepository;
import ru.otus.orlov.repositories.InterestRepository;
import ru.otus.orlov.repositories.RoleRepository;
import ru.otus.orlov.repositories.UserRepository;


/**
 * Реализация сервиса для работы с пользователями.
 * Этот класс предоставляет методы для поиска пользователей по идентификатору,
 * создания новых пользователей, а также обработки связанных сущностей (города и роли).
 *
 * @see UserService
 * @see User
 * @see UserDto
 * @see UserCreateDto
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    /**
     * Репозиторий для работы с данными пользователей.
     * Используется для выполнения операций CRUD (создание, чтение, обновление, удаление).
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий для работы с данными ролей.
     * Используется для поиска ролей, которые будут назначены пользователю.
     */
    private final RoleRepository roleRepository;

    /**
     * Репозиторий для работы с данными городов.
     * Используется для поиска или создания городов, связанных с пользователями.
     */
    private final CityRepository cityRepository;

    /**
     * Репозиторий для работы с данными интересов.
     * Используется для поиска или создания интересов, связанных с пользователями.
     */
    private final InterestRepository interestRepository;

    /**
     * Маппер для преобразования сущностей {@link User} в DTO {@link UserDto} и обратно.
     * Используется для отделения бизнес-логики от представления данных.
     */
    private final UserMapper userMapper;

    /**
     * Компонент для хеширования паролей.
     * Используется для безопасного хранения паролей пользователей в базе данных.
     */
    private final PasswordEncoder passwordEncoder;


    /**
     * Находит пользователя по его идентификатору и возвращает его DTO-представление.
     *
     * @param id идентификатор пользователя.
     * @return DTO-представление пользователя.
     * @throws NotFoundException если пользователь с указанным идентификатором не найден.
     */
    @Transactional(readOnly = true)
    @Override
    public UserDto findById(final Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Не удалось получить юзера по Id: %d", id)));
    }

    /**
     * Создает нового пользователя на основе данных из {@link UserCreateDto}
     *
     * @param userCreateDto DTO с данными для создания пользователя.
     * @return DTO-представление созданного пользователя.
     * @throws RoleNotFoundException если одна или несколько указанных ролей не найдены в базе данных.
     */
    @Transactional
    @Override
    public UserDto create(final UserCreateDto userCreateDto) {
        // Хешируем пароль перед сохранением
        final String encodedPassword = passwordEncoder.encode(userCreateDto.getPassword());
        // Получаем или создаем интересы
        final Set<Interest> interests = getOrCreateInterests(userCreateDto.getInterests());

        final User userToSave = new User(
                null,
                userCreateDto.getFirstName(),
                userCreateDto.getLastName(),
                userCreateDto.getBirthDate(),
                Gender.fromString(userCreateDto.getGender()),
                interests,
                getCity(userCreateDto),
                userCreateDto.getEmail(),
                encodedPassword,
                getRoles(userCreateDto),
                Boolean.TRUE,
                null,
                null,
                null
        );
        return userMapper.toDto(userRepository.save(userToSave));
    }

    /**
     * Реализация метода поиска пользователей по префиксу имени и фамилии.
     * Использует репозиторий для выполнения запроса к базе данных.
     *
     * @return список пользователей, удовлетворяющих условиям поиска.
     */
    @Transactional(readOnly = true)
    @Override
    public List<User> searchUsersByFirstNameAndLastName(final String firstName, final String lastName) {
        return userRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    /**
     * Получает или создает интересы на основе их описаний.
     *
     * @param descriptions набор описаний интересов
     * @return набор объектов {@link Interest}
     */
    private Set<Interest> getOrCreateInterests(final Set<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return Collections.emptySet();
        }
        final List<Interest> existingInterests = interestRepository.findByDescriptions(descriptions);
        final Set<Interest> interests = new HashSet<>(existingInterests);
        final Set<String> existingDescriptions = existingInterests.stream()
                .map(Interest::getDescription).collect(Collectors.toSet());
        final Set<String> newDescriptions = descriptions.stream()
                .filter(description -> !existingDescriptions.contains(description)).collect(Collectors.toSet());
        if (!newDescriptions.isEmpty()) {
            final Set<Interest> newInterests = newDescriptions.stream()
                    .map(description -> {
                        Interest interest = new Interest();
                        interest.setDescription(description);
                        return interest;
                    }).collect(Collectors.toSet());
            interestRepository.saveAll(newInterests);
            interests.addAll(newInterests);
        }
        return interests;
    }

    /**
     * Находит город по названию из {@link UserCreateDto}. Если город не найден, создает новый.
     *
     * @param userCreateDto DTO с данными для создания пользователя.
     * @return найденный или созданный город.
     */
    private City getCity(final UserCreateDto userCreateDto) {
        // Находим или создаем город
        return cityRepository.findByName(userCreateDto.getCity())
                .orElseGet(() -> {
                    // Если город не найден, создаем новый
                    City newCity = new City();
                    newCity.setName(userCreateDto.getCity());
                    return cityRepository.save(newCity); // Сохраняем новый город в БД
                });
    }

    /**
     * Находит роли по их названиям из {@link UserCreateDto}.
     * Если одна или несколько ролей не найдены, выбрасывает исключение {@link RoleNotFoundException}.
     *
     * @param userCreateDto DTO с данными для создания пользователя.
     * @return набор найденных ролей.
     * @throws RoleNotFoundException если одна или несколько указанных ролей не найдены в базе данных.
     */
    private Set<Role> getRoles(final UserCreateDto userCreateDto) {
        // Получаем роли из базы данных
        final Set<Role> roles = roleRepository.findByRoles(userCreateDto.getRole());

        // Проверяем, что все роли найдены
        if (roles.size() < userCreateDto.getRole().size()) {
            final Set<String> foundRoleNames = roles.stream()
                    .map(Role::getRole)
                    .collect(Collectors.toSet());

            final Set<String> missingRoles = userCreateDto.getRole().stream()
                    .filter(roleName -> !foundRoleNames.contains(roleName))
                    .collect(Collectors.toSet());
            throw new RoleNotFoundException("Следующие роли не найдены: " + missingRoles);
        }
        return roles;
    }
}
