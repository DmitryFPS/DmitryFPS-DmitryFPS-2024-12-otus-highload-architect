package ru.otus.orlov.listeners;

import java.util.Objects;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/** Слушатель для Очереди */
@Component
public class CacheUpdateListener {

    /** Менеджер кэша */
    private final CacheManager cacheManager;

    @Autowired
    public CacheUpdateListener(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /** Обработка сообщений из очереди */
    @RabbitListener(queues = "cacheUpdateQueue")
    public void receiveMessage(final Long userId) {
        // Очищаем кэш для конкретного userId
        Objects.requireNonNull(cacheManager.getCache("feed")).evict(userId);
        System.out.println("Кэш 'feed' для userId=" + userId + " очищен!");
    }
}
