package ru.xdxasoft.xdxanotes.activity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.services.PasswordValidationService;
import ru.xdxasoft.xdxanotes.utils.SessionManager;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText mail, pass;
    private Button btn;

    private ImageButton github_button, google_button, vk_button;


    private PasswordValidationService passwordValidationService;
    private SessionManager sessionManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean isSessionActive = getIntent().getBooleanExtra("ACCOUNT_VERI", false);
        String email = getIntent().getStringExtra("ACCOUNT_MAIL");
        if (isSessionActive == true) {
            ToastManager.showToast(this, "Письмо с подтверждением отправлено на " + email, R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);
        }

        // Инициализация сервисов
        passwordValidationService = new PasswordValidationService();
        sessionManager = new SessionManager(this);

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        auth = FirebaseAuth.getInstance();
        btn = findViewById(R.id.login_btn);
        mail = findViewById(R.id.logintext);
        pass = findViewById(R.id.passtext);
        github_button = findViewById(R.id.github_button);
        google_button = findViewById(R.id.google_button);
        vk_button = findViewById(R.id.vk_button);


        github_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGitHub();
            }
        });

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        vk_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                OAuthProvider.Builder provider = OAuthProvider.newBuilder("oidc.yandex");

                auth.startActivityForSignInWithProvider(LoginActivity.this, provider.build())
                        .addOnSuccessListener(authResult -> {
                            Log.d("YAauth", "Authentication successful");
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                Log.d("YAauth", "User UID: " + user.getUid());
                                Log.d("YAauth", "User Name: " + user.getDisplayName());
                                Log.d("YAauth", "User Email: " + user.getEmail());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("YAauth", "Authentication failed: " + e.getMessage());
                            if (e instanceof FirebaseAuthException) {
                                FirebaseAuthException authException = (FirebaseAuthException) e;
                                Log.e("YAauth", "Error code: " + authException.getErrorCode());
                                Log.e("YAauth", "Error message: " + authException.getMessage());
                            }
                        });
            }
        });

        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        pass.setOnEditorActionListener((v, actionId, event) -> {
            Log.d("LoginActivity", "Editor action triggered");
            if (actionId == EditorInfo.IME_ACTION_DONE || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                pass.clearFocus();
                btn.performClick();
                return true;
            }
            return false;
        });

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focusedView = getCurrentFocus();
                if (focusedView instanceof EditText) {
                    focusedView.clearFocus();
                    hideKeyboard();
                }
            }
            return false;
        });

        findViewById(R.id.main).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mail.isFocused() || pass.isFocused()) {
                    hideKeyboard();
                    mail.clearFocus();
                    pass.clearFocus();
                }
            }
            return false;
        });

        btn.setOnClickListener(v -> {
            if (mail.getText().toString().isEmpty() || pass.getText().toString().isEmpty()) {
                ToastManager.showToast(LoginActivity.this, "Введите логин и пароль!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
            } else {
                auth.signInWithEmailAndPassword(mail.getText().toString(), pass.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        if (user.isEmailVerified()) {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            boolean accountLogin = false;
                                            intent.putExtra("ACCOUNT_LOGIN", accountLogin);
                                            intent.putExtra("ACCOUNT_MAIL", mail.getText().toString());

                                            String hashedPassword = passwordValidationService.hashPassword(pass.getText().toString());
                                            sessionManager.savePasswordHash(hashedPassword); // Сохраняем хэш пароля в сессии
                                            passwordValidationService.saveLocalPasswordHash(pass.getText().toString()); // Сохраняем локально

                                            startActivity(intent);
                                            finish();
                                        } else {
                                            auth.signOut();
                                            ToastManager.showToast(LoginActivity.this, "Подтвердите почту!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                                        }
                                    }
                                } else {
                                    ToastManager.showToast(LoginActivity.this, "Неверный логин или пароль!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                                }
                            }
                        });
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mail.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(pass.getWindowToken(), 0);
        }
    }

    public void RegActivity(View v) {
        Intent intent = new Intent(LoginActivity.this, RegActivity.class);
        startActivity(intent);
    }

    public void RecoveryPassActivity(View v) {
        Intent intent = new Intent(LoginActivity.this, PasswordRecoveryActivity.class);
        startActivity(intent);
    }

    private void signInWithGitHub() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");

        List<String> scopes = new ArrayList<>();
        scopes.add("user:email");
        provider.setScopes(scopes);

        FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        Toast.makeText(this, "Аутентификация успешна: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGITHUBAUTH", e.getMessage());
                    Toast.makeText(this, "Ошибка аутентификации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void signInWithGoogle() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("google.com");

        List<String> scopes = new ArrayList<>();
        scopes.add("email");
        scopes.add("profile");
        provider.setScopes(scopes);

        FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        ToastManager.showToast(this, "Аутентификация успешна: " + user.getEmail(), R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("ACCOUNT_LOGIN", false);
                        intent.putExtra("ACCOUNT_MAIL", user.getEmail());
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGOOGLEAUTH", e.getMessage());
                    ToastManager.showToast(this, "Ошибка аутентификации: " + e.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                });
    }

    public void YAauth(View v) {
        Log.d("YAauth", "Yandex auth button clicked");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("oidc.yandex");

        auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    Log.d("YAauth", "Authentication successful");
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        Log.d("YAauth", "User Email: " + user.getEmail());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("YAauth", "Authentication failed: " + e.getMessage());
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        Log.e("YAauth", "Error code: " + authException.getErrorCode());
                        Log.e("YAauth", "Error message: " + authException.getMessage());
                    }
                });
    }

}
