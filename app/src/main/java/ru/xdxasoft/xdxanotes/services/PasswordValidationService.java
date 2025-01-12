package ru.xdxasoft.xdxanotes.services;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordValidationService {

    // Хэширование пароля с использованием SHA-256
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Сохранение хэшированного пароля локально (например, в SharedPreferences)
    public void saveLocalPasswordHash(String password) {
        // Логика сохранения пароля, например, в SharedPreferences
    }
}
