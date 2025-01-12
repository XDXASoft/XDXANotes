package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import java.util.Locale;

import ru.xdxasoft.xdxanotes.activity.MainActivity;

public class LocaleHelper {

    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "language";

    // Метод для установки языка
    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        // Обновляем конфигурацию
        Configuration config = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        // Сохраняем язык в SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
    }

    // Метод для получения текущего языка
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");  // по умолчанию английский
    }

    // Метод для применения языка при запуске приложения
    public static void applyLanguage(Context context) {
        String language = getLanguage(context);
        setLocale(context, language);
    }

    // Метод для перезапуска активности
    public static void restartActivity(Context context) {
        String language = getLanguage(context);
        setLocale(context, language);

        // Перезапускаем активность
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Метод для переключения между английским и русским языком
    public static void toggleLanguage(Context context) {
        String currentLanguage = getLanguage(context);
        String newLanguage = currentLanguage.equals("en") ? "ru" : "en"; // меняем язык
        setLocale(context, newLanguage);
    }
}
