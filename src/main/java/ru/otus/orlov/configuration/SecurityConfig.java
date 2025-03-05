package ru.otus.orlov.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.otus.orlov.handler.CustomAccessDeniedHandler;
import ru.otus.orlov.security.JwtRequestFilter;


/** Конфигурация безопасности Spring Security */
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    /** Фильтр для обработки запросов, содержащих JWT */
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Настраивает цепочку фильтров безопасности
     *
     * @param http объект для настройки безопасности
     * @return цепочка фильтров безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                // Отключает защиту от CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // Устанавливает политику создания сессий как без сохранения состояния
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Настраивает авторизацию запросов
                .authorizeHttpRequests((authorize) -> authorize
                        // Разрешает доступ к указанным путям без аутентификации
                        .requestMatchers(
                                "/authenticate", "/login", "/login-fail", "/logout", "/forbidden",
                                "/api/v1/login", "/api/v1/user/register", "/api/v1/logout", "/actuator/**"
                        ).permitAll()
                        // Требует аутентификации для всех остальных запросов
                        .anyRequest()
                        .authenticated())
                // Добавляет фильтр для обработки JWT перед фильтром аутентификации по имени пользователя и паролю
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // Настраивает обработку исключений, связанных с доступом
                .exceptionHandling(handling -> handling.accessDeniedHandler(new CustomAccessDeniedHandler()));
        // Возвращает настроенный объект безопасности
        return http.build();
    }

    /**
     * Создает кодировщик паролей BCrypt
     *
     * @return кодировщик паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Создает поставщик аутентификации на основе DAO
     *
     * @return поставщик аутентификации
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(final UserDetailsService userDetailsService,
                                                            final PasswordEncoder passwordEncoder) {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Создает экземпляр AuthenticationManager
     *
     * @param authConfig конфигурация аутентификации
     * @return экземпляр AuthenticationManager
     * @throws Exception если возникает ошибка при создании экземпляра AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
