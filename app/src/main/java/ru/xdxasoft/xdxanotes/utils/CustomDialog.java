package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import ru.xdxasoft.xdxanotes.R;

public class CustomDialog {

    public static void showCustomDialog(Context context, String dialogType, String message) {

        LayoutInflater inflater = LayoutInflater.from(context);
        int layoutId = getLayoutId(dialogType);
        View dialogView = inflater.inflate(layoutId, null);

        TextView messageText = dialogView.findViewById(R.id.messageText);
        messageText.setTextColor(ContextCompat.getColor(context, R.color.white));
        messageText.setText(message);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.CustomDialogStyle);

        builder.setView(dialogView)
                .setCancelable(true)
                .show();
    }

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