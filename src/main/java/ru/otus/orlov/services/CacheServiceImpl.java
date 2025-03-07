package ru.otus.orlov.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


/** Сервис кэша */
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    /** Менеджер кэша */
    private final CacheManager cacheManager;

    /** Проверить содержимое кэша */
    public Object getCachedValue(final String cacheName,
                                 final Object key) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            final Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return wrapper.get();
            }
        }
        return null;
    }
}
