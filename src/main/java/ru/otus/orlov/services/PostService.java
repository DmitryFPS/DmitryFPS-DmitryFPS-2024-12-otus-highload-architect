package ru.otus.orlov.services;

import ru.otus.orlov.dto.PageImplDto;
import ru.otus.orlov.entity.Post;

/** Сервис работы с постами */
public interface PostService {
    /** Получить посты друзей по ид */
    PageImplDto<Post> getFeed(final Long userId,
                              final int offset,
                              final int limit);

    /** Метод для обработки запросов из очереди */
    void processPostRequest(final byte[] messageBytes);
}
