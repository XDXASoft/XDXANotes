package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.Intent;

import ru.xdxasoft.xdxanotes.activity.SimpleDialogExampleActivity;
import ru.xdxasoft.xdxanotes.activity.AuthDialogExampleActivity;

/**
 * Утилитарный класс для запуска активностей с диалогами
 */
public class DialogLauncher {

    /**
     * Запускает активность с примерами упрощенных диалогов
     *
     * @param context Контекст, из которого запускается активность
     */
    public static void launchSimpleDialogExamples(Context context) {
        Intent intent = new Intent(context, SimpleDialogExampleActivity.class);
        context.startActivity(intent);
    }

    /**
     * Запускает активность с примером диалога авторизации
     *
     * @param context Контекст, из которого запускается активность
     */
    public static void launchAuthDialogExample(Context context) {
        Intent intent = new Intent(context, AuthDialogExampleActivity.class);
        context.startActivity(intent);
    }
}
