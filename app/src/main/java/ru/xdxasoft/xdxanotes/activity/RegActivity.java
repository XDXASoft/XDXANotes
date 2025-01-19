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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;

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
                // Проверяем уникальность имени пользователя перед регистрацией
                checkUsernameAndRegister(username, email, password);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mailreg.getWindowToken(), 0);
        }
    }

    private void checkUsernameAndRegister(String username, String email, String password) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = usersRef.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ToastManager.showToast(
                            RegActivity.this,
                            "Пользователь с таким именем уже зарегистрирован!",
                            R.drawable.ic_error,
                            Color.RED,
                            Color.BLACK,
                            Color.BLACK
                    );
                } else {
                    registerUser(email, password, username); // если имя уникально, продолжаем регистрацию
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastManager.showToast(
                        RegActivity.this,
                        "Ошибка проверки имени пользователя: " + databaseError.getMessage(),
                        R.drawable.ic_error,
                        Color.RED,
                        Color.BLACK,
                        Color.BLACK
                );
            }
        });
    }

    private void registerUser(String email, String password, String username) {
        mauth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Получаем UID пользователя
                            FirebaseUser user = mauth.getCurrentUser();
                            String userId = user.getUid();

                            // Ссылка на объект пользователя в базе данных
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                            userRef.child("email").setValue(email);
                            userRef.child("password").setValue(password);
                            userRef.child("username").setValue(username); // Записываем username

                            // Чтение последнего использованного ID из базы данных
                            DatabaseReference idRef = FirebaseDatabase.getInstance().getReference("LastUserId");
                            idRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Long lastId = dataSnapshot.getValue(Long.class);
                                    if (lastId == null) {
                                        lastId = 0L; // Если ID не найден, начинаем с 0
                                    }
                                    Long newId = lastId + 1; // Увеличиваем ID на 1

                                    // Сохраняем новый ID для пользователя
                                    userRef.child("id").setValue(newId);

                                    // Обновление последнего использованного ID в базе данных
                                    idRef.setValue(newId);

                                    // Переход на MainActivity после успешной регистрации
                                    Intent intent = new Intent(RegActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    ToastManager.showToast(RegActivity.this, "Ошибка при получении ID: " + databaseError.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                                }
                            });
                        } else {
                            ToastManager.showToast(RegActivity.this, "Не удалось зарегистрировать пользователя", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                        }
                    }
                });
    }

    private void showRegistrationError() {
        ToastManager.showToast(this, "Не удалось зарегистрировать пользователя", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
    }
}
