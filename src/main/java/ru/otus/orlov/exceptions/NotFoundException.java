package ru.otus.orlov.exceptions;

/**
 * Исключение, которое выбрасывается, когда запрашиваемый ресурс или объект не найден.
 * Это исключение является подклассом {@link RuntimeException}, что означает, что оно не
 * требует обязательной обработки в коде.
 * Это исключение может быть использовано в сервисном слое или контроллере для обработки ситуаций,
 * когда данные, запрошенные пользователем, отсутствуют в системе
 *
 * @see RuntimeException
 */
public class NotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке.
     *
     * @param message сообщение, которое описывает причину возникновения исключения.
     *                Обычно содержит информацию о том, какой ресурс не был найден.
     */
    public NotFoundException(final String message) {
        super(message);
    }
}
