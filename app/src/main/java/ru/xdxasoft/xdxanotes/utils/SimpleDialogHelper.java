package ru.xdxasoft.xdxanotes.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import ru.xdxasoft.xdxanotes.R;

/**
 * Упрощенный класс для создания и настройки кастомных диалогов
 */
public class SimpleDialogHelper {

    /**
     * Создает и показывает простой диалог с заголовком и сообщением
     */
    public static AlertDialog showSimpleDialog(
            Context context,
            String title,
            String message,
            String positiveText,
            DialogInterface.OnClickListener positiveClick,
            String negativeText,
            DialogInterface.OnClickListener negativeClick) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveClick)
                .setNegativeButton(negativeText, negativeClick);

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    /**
     * Создает и показывает диалог с полем ввода
     */
    public static Dialog showInputDialog(
            Context context,
            String title,
            String hint,
            String defaultText,
            String positiveText,
            DialogInterface.OnClickListener positiveClick,
            String negativeText,
            DialogInterface.OnClickListener negativeClick) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setTitle(title);

        // Создаем поле ввода
        final EditText input = new EditText(context);
        input.setHint(hint);
        input.setText(defaultText);
        builder.setView(input);

        // Добавляем кнопки
        builder.setPositiveButton(positiveText, (dialog, which) -> {
            if (positiveClick != null) {
                positiveClick.onClick(dialog, which);
            }
        });

        builder.setNegativeButton(negativeText, negativeClick);

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    /**
     * Интерфейс для получения введенного текста
     */
    public interface InputCallback {

        void onInput(String text);
    }

    /**
     * Создает и показывает диалог с полем ввода и callback для получения текста
     */
    public static Dialog showInputDialogWithCallback(
            Context context,
            String title,
            String hint,
            String defaultText,
            String positiveText,
            InputCallback callback,
            String negativeText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setTitle(title);

        // Создаем поле ввода
        final EditText input = new EditText(context);
        input.setHint(hint);
        input.setText(defaultText);
        builder.setView(input);

        // Добавляем кнопки
        builder.setPositiveButton(positiveText, (dialog, which) -> {
            if (callback != null) {
                callback.onInput(input.getText().toString());
            }
        });

        builder.setNegativeButton(negativeText, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
