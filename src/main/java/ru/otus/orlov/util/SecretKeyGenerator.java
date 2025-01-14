package ru.otus.orlov.util;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


/** Генерация секретного ключа */
public class SecretKeyGenerator {

    /** Получить секретный ключ */
    public static void getSecretKey() {

        try {
            // Создаем генератор ключей для алгоритма HMAC-SHA256
            final KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            keyGen.init(256); // Указываем длину ключа (256 бит)

            // Генерируем секретный ключ
            final SecretKey secretKey = keyGen.generateKey();

            // Кодируем ключ в Base64
            final String base64EncodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            // Выводим ключ
            System.out.println("Секретный ключ (Base64): " + base64EncodedKey);
        } catch (final NoSuchAlgorithmException e) {
            System.out.println("Не удалось сгенерировать секретный ключ (Base64):");
        }
    }
}
