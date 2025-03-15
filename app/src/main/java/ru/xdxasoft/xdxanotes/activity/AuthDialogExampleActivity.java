package ru.xdxasoft.xdxanotes.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.AuthDialogHelper;

public class AuthDialogExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_dialog_example);

        Button showAuthDialogButton = findViewById(R.id.show_auth_dialog_button);
        showAuthDialogButton.setOnClickListener(v -> showAuthDialog());
    }

    private void showAuthDialog() {
        AuthDialogHelper.showAuthDialog(
                this,
                "Авторизация",
                "Выберите способ авторизации:",
                new AuthDialogHelper.AuthDialogCallback() {
            @Override
            public void onGithubAuth() {
                Toast.makeText(AuthDialogExampleActivity.this,
                        "GitHub авторизация", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGoogleAuth() {
                Toast.makeText(AuthDialogExampleActivity.this,
                        "Google авторизация", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVkAuth() {
                Toast.makeText(AuthDialogExampleActivity.this,
                        "VK авторизация", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDialogClosed() {
                Toast.makeText(AuthDialogExampleActivity.this,
                        "Диалог закрыт", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
