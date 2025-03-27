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
import androidx.core.content.ContextCompat;

import ru.xdxasoft.xdxanotes.R;

public class CustomDialogHelper {

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

    public interface InputDialogCallback {
        void onInputReceived(String input);
    }

    public interface CustomDialogCallback {
        void onDialogCreated(Dialog dialog, View dialogView);
    }

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
                ContextCompat.getColor(context, R.color.primary),
                positiveClickListener,
                negativeButtonText,
                ContextCompat.getColor(context, R.color.secondary),
                negativeClickListener
        );
    }
}