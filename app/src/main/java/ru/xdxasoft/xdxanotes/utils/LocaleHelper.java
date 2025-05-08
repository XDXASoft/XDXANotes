package ru.xdxasoft.xdxanotes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Locale;

import ru.xdxasoft.xdxanotes.activity.MainActivity;

public class LocaleHelper {

    private static final String TAG = "LocaleHelper";
    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_USE_SYSTEM_LANGUAGE = "use_system_language";

    /**
     * Устанавливает язык приложения
     *
     * @param context Контекст приложения
     * @param languageCode Код языка (ru, en)
     * @return Обновленный контекст с новой локалью
     */
    public static Context setLocale(Context context, String languageCode) {
        try {
            Log.d(TAG, "Установка языка: " + languageCode);

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LANGUAGE, languageCode);
            editor.apply();

            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);

            Resources resources = context.getResources();
            Configuration config = new Configuration(resources.getConfiguration());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
                context = context.createConfigurationContext(config);
            } else {
                config.locale = locale;
                resources.updateConfiguration(config, resources.getDisplayMetrics());
            }

            return context;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при установке языка: " + e.getMessage(), e);
            return context;
        }
    }

    /**
     * Получает текущий язык приложения, учитывая настройку использования языка
     * системы
     *
     * @param context Контекст приложения
     * @return Код языка (ru, en)
     */
    public static String getLanguage(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            boolean useSystemLanguage = prefs.getBoolean(KEY_USE_SYSTEM_LANGUAGE, true);

            if (useSystemLanguage) {
                String systemLanguage = getSystemLanguage();
                Log.d(TAG, "Используется язык системы: " + systemLanguage);
                return systemLanguage;
            }

            String savedLanguage = prefs.getString(KEY_LANGUAGE, getSystemLanguage());
            Log.d(TAG, "Используется сохраненный язык: " + savedLanguage);
            return savedLanguage;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении языка: " + e.getMessage(), e);
            return "ru";
        }
    }

    /**
     * Проверяет, использует ли приложение язык системы
     *
     * @param context Контекст приложения
     * @return true, если используется язык системы
     */
    public static boolean isUsingSystemLanguage(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_USE_SYSTEM_LANGUAGE, true);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке использования языка системы: " + e.getMessage(), e);
            return true;
        }
    }

    /**
     * Определяет язык системы
     *
     * @return "ru" если язык системы русский, иначе "en"
     */
    public static String getSystemLanguage() {
        try {
            String systemLanguage = Locale.getDefault().getLanguage();
            Log.d(TAG, "Язык системы: " + systemLanguage);

            return systemLanguage.equals("ru") ? "ru" : "en";
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при определении языка системы: " + e.getMessage(), e);
            return "ru";
        }
    }

    /**
     * Применяет сохраненный язык при запуске приложения
     *
     * @param context Контекст приложения
     * @return Обновленный контекст с новой локалью
     */
    public static Context applyLanguage(Context context) {
        try {
            String language = getLanguage(context);
            Log.d(TAG, "Применение языка: " + language);
            return setLocale(context, language);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при применении языка: " + e.getMessage(), e);
            return context;
        }
    }

    /**
     * Перезапускает активность для применения изменений языка
     *
     * @param activity Активность для перезапуска
     */
    public static void restartActivity(Activity activity) {
        try {
            if (activity == null) {
                Log.e(TAG, "Активность равна null, невозможно перезапустить");
                return;
            }

            String language = getLanguage(activity);
            Log.d(TAG, "Перезапуск активности с языком: " + language);

            setLocale(activity, language);

            Intent intent = activity.getIntent();
            activity.finish();
            activity.overridePendingTransition(0, 0);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при перезапуске активности: " + e.getMessage(), e);
        }
    }

    /**
     * Переключает язык между русским и английским и перезапускает активность
     *
     * @param activity Активность для перезапуска
     */
    public static void toggleLanguage(Activity activity) {
        try {
            if (activity == null) {
                Log.e(TAG, "Активность равна null, невозможно переключить язык");
                return;
            }

            String currentLanguage = getLanguage(activity);
            String newLanguage = currentLanguage.equals("en") ? "ru" : "en";

            Log.d(TAG, "Переключение языка с " + currentLanguage + " на " + newLanguage);

            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LANGUAGE, newLanguage);
            editor.putBoolean(KEY_USE_SYSTEM_LANGUAGE, false);
            editor.apply();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            }, 100);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при переключении языка: " + e.getMessage(), e);
        }
    }

    /**
     * Устанавливает использование языка системы
     *
     * @param activity Активность для перезапуска
     */
    public static void useSystemLanguage(Activity activity) {
        try {
            if (activity == null) {
                Log.e(TAG, "Активность равна null, невозможно установить язык системы");
                return;
            }

            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_USE_SYSTEM_LANGUAGE, true);
            editor.apply();

            String systemLanguage = getSystemLanguage();
            Log.d(TAG, "Установка языка системы: " + systemLanguage);

            restartActivity(activity);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при установке языка системы: " + e.getMessage(), e);
        }
    }
}
