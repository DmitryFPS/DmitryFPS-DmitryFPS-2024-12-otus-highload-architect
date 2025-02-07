package ru.otus.orlov.model;

import lombok.Data;

/**
 * Класс, представляющий запрос на выход из системы.
 * Используется для передачи данных, необходимых для завершения сеанса пользователя.
 */
@Data
public class LogoutRequest {
    /** Электронная почта пользователя, который выполняет выход из системы */
    private String email;
}
