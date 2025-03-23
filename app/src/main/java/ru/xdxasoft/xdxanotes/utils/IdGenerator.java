package ru.xdxasoft.xdxanotes.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class IdGenerator {

    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();


    public static String generateRandomId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            sb.append(ALLOWED_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }


    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
