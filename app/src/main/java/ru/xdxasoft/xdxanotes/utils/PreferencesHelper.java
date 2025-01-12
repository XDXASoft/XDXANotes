package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class PreferencesHelper {

    private static final String PREF_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";

    // Возвращает выбранный язык
    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, "en"); // по умолчанию - английский
    }

    // Сохраняет выбранный язык
    public static void setLanguage(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    // Возвращает выбранную тему
    public static String getTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_THEME, "light"); // по умолчанию - светлая тема
    }

    // Сохраняет выбранную тему
    public static void setTheme(Context context, String theme) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }
}
