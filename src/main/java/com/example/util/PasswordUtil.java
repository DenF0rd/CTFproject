package com.example.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Хэширует пароль с помощью BCrypt
     * @param plainPassword обычный пароль
     * @return хэшированный пароль
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Проверяет, соответствует ли пароль хэшу
     * @param plainPassword обычный пароль
     * @param hashedPassword хэшированный пароль из БД
     * @return true если пароль верный
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}