package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import ru.xdxasoft.xdxanotes.R;

public class CustomDialog {

    // Метод для отображения кастомного диалога
    public static void showCustomDialog(Context context, String dialogType, String message) {

        // Инфлейтим выбранный макет диалога
        LayoutInflater inflater = LayoutInflater.from(context);
        int layoutId = getLayoutId(dialogType); // Получаем ID макета в зависимости от типа
        View dialogView = inflater.inflate(layoutId, null); // Инфлейтим макет

        // Настройка текста сообщения
        TextView messageText = dialogView.findViewById(R.id.messageText);
        messageText.setTextColor(Color.WHITE);
        messageText.setText(message);

        // Создание и показ диалога с использованием Material Design и кастомного стиля
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.CustomDialogStyle);

        builder.setView(dialogView)
                .setCancelable(true)
                .show();
    }

    // Метод для выбора макета в зависимости от типа диалога
    private static int getLayoutId(String dialogType) {
        switch (dialogType) {
            case "custom_dialog":
                return R.layout.dialog_custom;
            case "w1":
                return R.layout.w1;
            default:
                return R.layout.dialog_custom;
        }
    }
}