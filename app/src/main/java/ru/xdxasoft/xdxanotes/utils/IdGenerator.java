package ru.xdxasoft.xdxanotes.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class IdGenerator {

    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALLOWED_SPECIAL_CHARS = "!@%^&*()-_=+{}:,<>?";
    private static final String SAFE_CHARS = LOWERCASE_CHARS + UPPERCASE_CHARS + DIGITS + ALLOWED_SPECIAL_CHARS;

    private static final int COMPLEX_ID_LENGTH = 32;
    private static final int ID_LENGTH = 16;

    private static final SecureRandom random = new SecureRandom();


    public static String generateRandomId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int randomIndex = random.nextInt(LOWERCASE_CHARS.length() + UPPERCASE_CHARS.length() + DIGITS.length());
            if (randomIndex < LOWERCASE_CHARS.length()) {
                sb.append(LOWERCASE_CHARS.charAt(randomIndex));
            } else if (randomIndex < LOWERCASE_CHARS.length() + UPPERCASE_CHARS.length()) {
                sb.append(UPPERCASE_CHARS.charAt(randomIndex - LOWERCASE_CHARS.length()));
            } else {
                sb.append(DIGITS.charAt(randomIndex - LOWERCASE_CHARS.length() - UPPERCASE_CHARS.length()));
            }
        }
        return sb.toString();
    }


    public static String generateComplexId() {
        StringBuilder sb = new StringBuilder(COMPLEX_ID_LENGTH);

        sb.append(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        sb.append(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        sb.append(ALLOWED_SPECIAL_CHARS.charAt(random.nextInt(ALLOWED_SPECIAL_CHARS.length())));

        for (int i = 4; i < COMPLEX_ID_LENGTH; i++) {
            sb.append(SAFE_CHARS.charAt(random.nextInt(SAFE_CHARS.length())));
        }

        char[] chars = sb.toString().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int randomIndex = random.nextInt(chars.length);
            char temp = chars[i];
            chars[i] = chars[randomIndex];
            chars[randomIndex] = temp;
        }

        return new String(chars);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
