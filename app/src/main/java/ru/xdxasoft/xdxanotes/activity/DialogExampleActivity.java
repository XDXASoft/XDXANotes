package ru.xdxasoft.xdxanotes.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.CustomDialogHelper;


public class DialogExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_example);

        Button simpleDialogButton = findViewById(R.id.simple_dialog_button);
        simpleDialogButton.setOnClickListener(v -> showSimpleDialog());

        Button inputDialogButton = findViewById(R.id.input_dialog_button);
        inputDialogButton.setOnClickListener(v -> showInputDialog());

        Button customDialogButton = findViewById(R.id.custom_dialog_button);
        customDialogButton.setOnClickListener(v -> showCustomDialog());
    }


    private void showSimpleDialog() {
        CustomDialogHelper.showSimpleDialog(
                this,
                "Простой диалог",
                "Это пример простого диалога с заголовком и сообщением.",
                "ОК",
                (dialog, which) -> Toast.makeText(this, "Нажата кнопка ОК", Toast.LENGTH_SHORT).show(),
                "Отмена",
                (dialog, which) -> dialog.dismiss()
        );
    }

    private void showInputDialog() {
        CustomDialogHelper.showInputDialog(
                this,
                "Ввод текста",
                "Введите что-нибудь",
                "",
                "Сохранить",
                "Отмена",
                input -> Toast.makeText(this, "Введено: " + input, Toast.LENGTH_SHORT).show()
        );
    }


    private void showCustomDialog() {
        CustomDialogHelper.showCustomDialog(this, (dialog, dialogView) -> {
            TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
            TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
            EditText editText = dialogView.findViewById(R.id.dialog_edit_text);
            Button positiveButton = dialogView.findViewById(R.id.dialog_positive_button);
            Button negativeButton = dialogView.findViewById(R.id.dialog_negative_button);

            titleTextView.setText("Настраиваемый диалог");
            messageTextView.setText("Этот диалог можно полностью настроить");
            editText.setHint("Настраиваемое поле ввода");

            positiveButton.setText("Готово");
            positiveButton.setOnClickListener(v -> {
                String inputText = editText.getText().toString();
                Toast.makeText(this, "Введено: " + inputText, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            negativeButton.setText("Закрыть");
            negativeButton.setOnClickListener(v -> dialog.dismiss());
        });
    }
}
