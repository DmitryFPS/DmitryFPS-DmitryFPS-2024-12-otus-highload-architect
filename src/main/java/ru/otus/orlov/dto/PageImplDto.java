package ru.otus.orlov.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс, представляющий страницу данных с пагинацией.
 * Используется для передачи данных между слоями приложения, содержащих список элементов,
 * информацию о текущей странице, размере страницы и общем количестве элементов.
 *
 * @param <T> Тип элементов, содержащихся на странице.
 */
@Getter
@Setter
public class PageImplDto<T> {
    /** Список элементов на текущей странице. */
    private List<T> content;

    /** Номер текущей страницы (начиная с 0). */
    private int pageNumber;

    /** Размер страницы (количество элементов на странице). */
    private int pageSize;

    /** Общее количество элементов во всех страницах. */
    private long totalElements;
}
