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
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;
import ru.xdxasoft.xdxanotes.utils.AuthManager;

public class RegActivity extends AppCompatActivity {

    private Button regbtn;
    private EditText mailreg, passreg, usernamereg;
    private FirebaseAuth mauth;
    private AuthManager authManager;
    private ProgressBar progressBar;

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
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager();

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
                    showLoading(true);
                    authManager.registerUser(email, password, username, new AuthManager.OnRegistrationListener() {
                        @Override
                        public void onSuccess(String message) {
                            showLoading(false);
                            ToastManager.showToast(
                                    RegActivity.this,
                                    message,
                                    R.drawable.ic_galohca_black,
                                    Color.GREEN,
                                    Color.BLACK,
                                    Color.BLACK
                            );
                            // Переход на экран входа
                            Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            showLoading(false);
                            ToastManager.showToast(
                                    RegActivity.this,
                                    error,
                                    R.drawable.ic_error,
                                    Color.RED,
                                    Color.BLACK,
                                    Color.BLACK
                            );
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
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

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        regbtn.setEnabled(!show);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
