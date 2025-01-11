package ru.otus.orlov.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.orlov.repositories.UserRepository;


/**
 * Реализация интерфейса {@link UserDetailsService}, которая предоставляет механизм
 * для загрузки данных пользователя по его email.
 *
 * @see UserDetailsService
 * @see UserRepository
 */
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    /**
     * Репозиторий для работы с данными пользователей.
     * Используется для поиска пользователей в базе данных по их email.
     * Внедряется через конструктор благодаря аннотации {@link RequiredArgsConstructor}.
     *
     * @see UserRepository
     */
    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по его email.
     * Метод выполняет поиск пользователя в базе данных с использованием репозитория {@link UserRepository}.
     * Если пользователь с указанным email не найден, выбрасывается исключение {@link UsernameNotFoundException}.
     *
     * @param email email пользователя, данные которого необходимо загрузить
     * @return объект {@link UserDetails}, содержащий информацию о пользователе
     * @throws UsernameNotFoundException если пользователь с указанным email не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Пользователь с email %s не найден".formatted(email)));
    }
}
