package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_THEME = "selected_theme";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    // Сохраняем выбранную тему
    public static void saveTheme(Context context, int theme) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_THEME, theme).apply();
    }

    // Получаем сохранённую тему
    public static int getTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_THEME, THEME_SYSTEM); // По умолчанию - системная тема
    }

    // Применяем тему
    public static void applyTheme(Context context) {

        int theme = getTheme(context);
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
