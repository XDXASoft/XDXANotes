package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.Intent;

import ru.xdxasoft.xdxanotes.activity.SimpleDialogExampleActivity;
import ru.xdxasoft.xdxanotes.activity.AuthDialogExampleActivity;

public class DialogLauncher {


    public static void launchSimpleDialogExamples(Context context) {
        Intent intent = new Intent(context, SimpleDialogExampleActivity.class);
        context.startActivity(intent);
    }


    public static void launchAuthDialogExample(Context context) {
        Intent intent = new Intent(context, AuthDialogExampleActivity.class);
        context.startActivity(intent);
    }
}
