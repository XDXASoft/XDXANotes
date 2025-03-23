package ru.xdxasoft.xdxanotes.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import ru.xdxasoft.xdxanotes.R;

public class AuthDialogHelper {


    public interface AuthDialogCallback {

        void onGithubAuth();

        void onGoogleAuth();

        void onVkAuth();

        void onDialogClosed();
    }

    /**
     * Показывает диалог авторизации через социальные сети
     *
     * @param context Контекст
     * @param title Заголовок диалога
     * @param message Сообщение диалога
     * @param callback Обработчик нажатий на кнопки
     * @return Созданный диалог
     */
    public static Dialog showAuthDialog(
            Context context,
            String title,
            String message,
            AuthDialogCallback callback) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.auth_dialog_layout, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        ImageButton closeButton = dialogView.findViewById(R.id.close_button);
        ImageButton githubButton = dialogView.findViewById(R.id.github_button);
        ImageButton googleButton = dialogView.findViewById(R.id.google_button);
        ImageButton vkButton = dialogView.findViewById(R.id.vk_button);

        if (title != null) {
            titleTextView.setText(title);
        }

        if (message != null) {
            messageTextView.setText(message);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setView(dialogView);

        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onDialogClosed();
            }
            dialog.dismiss();
        });

        githubButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onGithubAuth();
            }
            dialog.dismiss();
        });

        googleButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onGoogleAuth();
            }
            dialog.dismiss();
        });

        vkButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onVkAuth();
            }
            dialog.dismiss();
        });

        dialog.show();
        return dialog;
    }
}
