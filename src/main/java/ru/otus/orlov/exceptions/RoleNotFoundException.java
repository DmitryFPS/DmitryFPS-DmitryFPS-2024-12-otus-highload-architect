package ru.otus.orlov.exceptions;

/**
 * Исключение, которое выбрасывается, если указанные роли не найдены в системе.
 * Используется для обработки ситуаций, когда пользователь пытается создать или обновить
 * сущность с ролями, которые отсутствуют в базе данных.
 *
 * @see RuntimeException
 */
public class RoleNotFoundException extends RuntimeException {
    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message Сообщение, описывающее причину возникновения исключения
     *                Обычно содержит информацию о том, какие роли не найдены
     */
    public RoleNotFoundException(final String message) {
        super(message);
    }
}
