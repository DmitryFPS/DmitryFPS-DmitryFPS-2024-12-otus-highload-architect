package ru.otus.orlov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.orlov.services.CacheService;

/** Контроллер для кэша */
@RestController
@RequiredArgsConstructor
public class CacheController {
    /** Сервис кэша */
    private final CacheService cacheService;

    @GetMapping("/api/v1/cache/{userId}")
    public Object getCachedFeed(@PathVariable final Long userId) {
        return cacheService.getCachedValue("feed", userId);
    }
}
