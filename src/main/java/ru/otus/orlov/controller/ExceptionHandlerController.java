package ru.otus.orlov.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.otus.orlov.dto.ErrorDto;
import ru.otus.orlov.exceptions.NotFoundException;
import ru.otus.orlov.exceptions.RoleNotFoundException;


/**
 * Глобальный обработчик исключений для REST-контроллеров
 * Этот класс перехватывает исключения, возникающие в контроллерах, и возвращает
 * соответствующие HTTP-ответы с информацией об ошибке
 * Логирует все перехваченные исключения с помощью SLF4J
 *
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see ResponseStatus
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {

    /**
     * Обрабатывает исключение {@link NotFoundException}.
     * Возвращает HTTP-ответ со статусом 404 (Not Found) и сообщением об ошибке
     *
     * @param ex исключение типа {@link NotFoundException}, которое было перехвачено
     * @return объект {@link ErrorDto}, содержащий статус и сообщение об ошибке
     * @see NotFoundException
     * @see ErrorDto
     * @see HttpStatus#NOT_FOUND
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorDto handleNotFound(final NotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return getError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Обрабатывает исключение {@link RoleNotFoundException}, которое возникает, если указанные роли не найдены.
     * Возвращает объект {@link ErrorDto} с информацией об ошибке и статусом HTTP 400 (Bad Request)
     *
     * @param ex Исключение {@link RoleNotFoundException}, содержащее информацию о недостающих ролях
     * @return Объект {@link ErrorDto}, содержащий информацию об ошибке
     * @see RoleNotFoundException
     * @see ErrorDto
     * @see HttpStatus#BAD_REQUEST
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RoleNotFoundException.class)
    public ErrorDto handleRoleNotFound(final RoleNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return getError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Обрабатывает все неперехваченные исключения.
     * Возвращает HTTP-ответ со статусом 500 (Internal Server Error) и сообщением об ошибке
     *
     * @param ex исключение типа {@link Exception}, которое было перехвачено.
     * @return объект {@link ErrorDto}, содержащий статус и сообщение об ошибке.
     * @see Exception
     * @see ErrorDto
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorDto handleServerError(final Exception ex) {
        log.error(ex.getMessage(), ex);
        return getError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * Создает объект {@link ErrorDto} на основе переданного статуса и сообщения.
     * Используется для формирования ответа с ошибкой
     *
     * @param status  HTTP-статус, который будет возвращен в ответе
     * @param message сообщение об ошибке, которое будет возвращено в ответе
     * @return объект {@link ErrorDto}, содержащий статус и сообщение об ошибке
     * @see ErrorDto
     * @see HttpStatus
     */
    private ErrorDto getError(final HttpStatus status, final String message) {
        final ErrorDto error = new ErrorDto();
        error.setStatusCode(status);
        error.setMessage(message);
        return error;
    }
}
