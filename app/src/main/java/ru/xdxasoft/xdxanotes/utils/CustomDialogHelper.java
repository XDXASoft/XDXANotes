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
 * Вспомогательный класс для создания и настройки кастомных диалогов
 */
public class CustomDialogHelper {

    /**
     * Создает и показывает простой диалог с заголовком и сообщением
     *
     * @param context Контекст
     * @param title Заголовок диалога
     * @param message Сообщение диалога
     * @param positiveText Текст положительной кнопки
     * @param positiveColor Цвет положительной кнопки
     * @param positiveClick Обработчик нажатия положительной кнопки
     * @param negativeText Текст отрицательной кнопки
     * @param negativeColor Цвет отрицательной кнопки
     * @param negativeClick Обработчик нажатия отрицательной кнопки
     * @return Созданный диалог
     */
    public static AlertDialog showSimpleDialog(
            Context context,
            String title,
            String message,
            String positiveText,
            int positiveColor,
            DialogInterface.OnClickListener positiveClick,
            String negativeText,
            int negativeColor,
            DialogInterface.OnClickListener negativeClick) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveClick)
                .setNegativeButton(negativeText, negativeClick);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(positiveColor);
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(negativeColor);
            }
        });

        dialog.show();
        return dialog;
    }

    /**
     * Создает и показывает диалог с полем ввода
     *
     * @param context Контекст
     * @param title Заголовок диалога
     * @param hint Подсказка для поля ввода
     * @param defaultText Текст по умолчанию в поле ввода
     * @param positiveText Текст положительной кнопки
     * @param negativeText Текст отрицательной кнопки
     * @param callback Обработчик результата ввода
     * @return Созданный диалог
     */
    public static Dialog showInputDialog(
            Context context,
            String title,
            String hint,
            String defaultText,
            String positiveText,
            String negativeText,
            final InputDialogCallback callback) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        EditText editText = dialogView.findViewById(R.id.dialog_edit_text);
        Button positiveButton = dialogView.findViewById(R.id.dialog_positive_button);
        Button negativeButton = dialogView.findViewById(R.id.dialog_negative_button);

        titleTextView.setText(title);
        messageTextView.setVisibility(View.GONE);
        editText.setHint(hint);

        if (defaultText != null && !defaultText.isEmpty()) {
            editText.setText(defaultText);
            editText.setSelection(defaultText.length());
        }

        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        positiveButton.setOnClickListener(v -> {
            String inputText = editText.getText().toString();
            if (callback != null) {
                callback.onInputReceived(inputText);
            }
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    /**
     * Создает и показывает полностью настраиваемый диалог
     *
     * @param context Контекст
     * @param callback Обработчик для настройки диалога
     * @return Созданный диалог
     */
    public static Dialog showCustomDialog(Context context, CustomDialogCallback callback) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        if (callback != null) {
            callback.onDialogCreated(dialog, dialogView);
        }

        dialog.show();
        return dialog;
    }

    /**
     * Интерфейс обратного вызова для диалога с вводом
     */
    public interface InputDialogCallback {

        void onInputReceived(String input);
    }

    /**
     * Интерфейс обратного вызова для настраиваемого диалога
     */
    public interface CustomDialogCallback {

        void onDialogCreated(Dialog dialog, View dialogView);
    }

    /**
     * Перегруженный метод для обратной совместимости
     *
     * @param context Контекст
     * @param title Заголовок диалога
     * @param message Сообщение диалога
     * @param positiveButtonText Текст положительной кнопки
     * @param positiveClickListener Обработчик нажатия положительной кнопки
     * @param negativeButtonText Текст отрицательной кнопки
     * @param negativeClickListener Обработчик нажатия отрицательной кнопки
     */
    public static void showSimpleDialog(
            Context context,
            String title,
            String message,
            String positiveButtonText,
            DialogInterface.OnClickListener positiveClickListener,
            String negativeButtonText,
            DialogInterface.OnClickListener negativeClickListener
    ) {
        showSimpleDialog(
                context,
                title,
                message,
                positiveButtonText,
                context.getResources().getColor(R.color.primary),
                positiveClickListener,
                negativeButtonText,
                context.getResources().getColor(R.color.secondary),
                negativeClickListener
        );
    }
}
