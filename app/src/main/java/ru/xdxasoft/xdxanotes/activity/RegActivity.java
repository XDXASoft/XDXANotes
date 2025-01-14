package ru.xdxasoft.xdxanotes.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class RegActivity extends AppCompatActivity {

    private Button regbtn;
    private EditText mailreg, passreg, usernamereg;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        regbtn = findViewById(R.id.reg_btn);
        mailreg = findViewById(R.id.logintext);
        passreg = findViewById(R.id.passtext);
        usernamereg = findViewById(R.id.usernametext);
        mauth = FirebaseAuth.getInstance();

        passreg.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                regbtn.performClick();
                return true;
            }
            return false;
        });

        regbtn.setOnClickListener(v -> {
            String email = mailreg.getText().toString().trim();
            String password = passreg.getText().toString().trim();
            String username = usernamereg.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                ToastManager.showToast(this, "Введите имя пользователя, логин и пароль!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
            } else {
                registerUser(email, password, username);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mailreg.getWindowToken(), 0);
        }
    }

    private void registerUser(String email, String password, String username) {
        mauth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mauth.getCurrentUser();
                        if (user != null) {
                            // Отправка письма подтверждения
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {


                                    // Возврат на экран входа
                                    Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                                    intent.putExtra("ACCOUNT_VERI", true);

                                    intent.putExtra("ACCOUNT_MAIL", mailreg.getText().toString());
                                    startActivity(intent);
                                } else {
                                    ToastManager.showToast(this, "Ошибка отправки письма подтверждения", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                                }
                            });
                        } else {
                            showRegistrationError();
                        }
                    } else {
                        showRegistrationError();
                    }
                });
    }

    private void showRegistrationError() {
        ToastManager.showToast(this, "Не удалось зарегистрировать пользователя", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
    }

    private void checkEmailVerification() {
        FirebaseUser user = mauth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    ToastManager.showToast(this, "Почта успешно подтверждена!", R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);

                    // Переход в MainActivity
                    Intent intent = new Intent(RegActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Почта не подтверждена
                    ToastManager.showToast(this, "Пожалуйста, подтвердите вашу почту!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                }
            });
        }
    }
}
