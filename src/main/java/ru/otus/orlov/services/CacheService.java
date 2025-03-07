package ru.otus.orlov.services;

/** Сервис кэша */
public interface CacheService {
    /** Проверить содержимое кэша */
    Object getCachedValue(final String cacheName,
                          final Object key);
}
