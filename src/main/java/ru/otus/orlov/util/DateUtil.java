package ru.otus.orlov.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Для работы со временем */
public class DateUtil {
    /** Конвертируем Date в LocalDateTime */
    public static LocalDateTime asLocalDateTime(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
