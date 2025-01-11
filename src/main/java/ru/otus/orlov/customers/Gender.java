package ru.otus.orlov.customers;

import java.util.Locale;

/** Перечисление, представляющее пол */
public enum Gender {
    /** Мужской пол */
    MALE,

    /** Женский пол */
    FEMALE
    ;

    /**
     * Метод для преобразования строки в значение enum Gender
     *
     * @param genderString строка, представляющая пол (например, "MALE" или "FEMALE")
     * @return соответствующее значение enum Gender
     * @throws IllegalArgumentException если строка не соответствует ни одному из значений enum
     */
    public static Gender fromString(final String genderString) {
        if (genderString == null) {
            throw new IllegalArgumentException("Gender string cannot be null");
        }

        // Проверяем, соответствует ли строка одному из значений enum
        for (final Gender gender : Gender.values()) {
            if (gender.name().equals(genderString.toUpperCase(Locale.ROOT))) {
                return gender;
            }
        }

        // Если строка не соответствует ни одному значению, выбрасываем исключение
        throw new IllegalArgumentException("Unknown gender: " + genderString);
    }
}
