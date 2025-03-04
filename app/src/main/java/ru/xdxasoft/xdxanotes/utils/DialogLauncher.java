package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.Intent;

import ru.xdxasoft.xdxanotes.activity.SimpleDialogExampleActivity;

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
}
