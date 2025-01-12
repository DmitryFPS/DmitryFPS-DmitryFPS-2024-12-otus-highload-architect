package ru.otus.orlov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;


/**
 * Класс, представляющий объект ошибки.
 * Используется для передачи информации об ошибке в REST-ответах.
 *
 * @see HttpStatusCode
 * @see Data
 * @see AllArgsConstructor
 * @see NoArgsConstructor
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDto {
    /**
     * HTTP-статус ошибки.
     * Используется для указания статуса ответа, например, 404 (Not Found) или 500 (Internal Server Error).
     *
     * @see HttpStatusCode
     */
    private HttpStatusCode statusCode;

    /**
     * Сообщение об ошибке.
     * Содержит текстовое описание ошибки, которое может быть полезно для клиента.
     */
    private String message;
}
