package ru.xdxasoft.xdxanotes.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.SimpleDialogHelper;

/**
 * Пример использования упрощенных кастомных диалогов
 */
public class SimpleDialogExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dialog_example);

        // Кнопка для показа простого диалога
        Button simpleDialogButton = findViewById(R.id.simple_dialog_button);
        simpleDialogButton.setOnClickListener(v -> showSimpleDialog());

        // Кнопка для показа диалога с вводом
        Button inputDialogButton = findViewById(R.id.input_dialog_button);
        inputDialogButton.setOnClickListener(v -> showInputDialog());

        // Кнопка для показа диалога с вводом и callback
        Button inputCallbackDialogButton = findViewById(R.id.input_callback_dialog_button);
        inputCallbackDialogButton.setOnClickListener(v -> showInputDialogWithCallback());
    }

    /**
     * Показывает простой диалог с заголовком и сообщением
     */
    private void showSimpleDialog() {
        SimpleDialogHelper.showSimpleDialog(
                this,
                "Простой диалог",
                "Это пример простого диалога с заголовком и сообщением. Теперь без наслоения элементов.",
                "ОК",
                (dialog, which) -> Toast.makeText(this, "Нажата кнопка ОК", Toast.LENGTH_SHORT).show(),
                "Отмена",
                (dialog, which) -> dialog.dismiss()
        );
    }

    /**
     * Показывает диалог с полем ввода
     */
    private void showInputDialog() {
        SimpleDialogHelper.showInputDialog(
                this,
                "Ввод текста",
                "Введите что-нибудь",
                "",
                "Сохранить",
                (dialog, which) -> {
                    // Здесь можно получить текст из диалога, если нужно
                    dialog.dismiss();
                },
                "Отмена",
                (dialog, which) -> dialog.dismiss()
        );
    }

    /**
     * Показывает диалог с полем ввода и callback для получения текста
     */
    private void showInputDialogWithCallback() {
        SimpleDialogHelper.showInputDialogWithCallback(
                this,
                "Ввод с обратным вызовом",
                "Введите текст",
                "",
                "Готово",
                text -> Toast.makeText(this, "Введено: " + text, Toast.LENGTH_SHORT).show(),
                "Отмена"
        );
    }
}
