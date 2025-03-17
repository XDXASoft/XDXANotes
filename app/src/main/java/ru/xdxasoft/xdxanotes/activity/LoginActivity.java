package ru.xdxasoft.xdxanotes.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.services.PasswordValidationService;
import ru.xdxasoft.xdxanotes.utils.AuthManager;
import ru.xdxasoft.xdxanotes.utils.CustomDialogHelper;
import ru.xdxasoft.xdxanotes.utils.SessionManager;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText mail, pass;
    private Button btn;

    private ImageButton github_button, google_button, vk_button;

    private PasswordValidationService passwordValidationService;
    private SessionManager sessionManager;
    private AuthManager authManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        boolean isVerificationSent = getIntent().getBooleanExtra("ACCOUNT_VERI", false);
        String emailFromIntent = getIntent().getStringExtra("ACCOUNT_MAIL");

        if (isVerificationSent && emailFromIntent != null) {
            ToastManager.showToast(this,
                    "Письмо с подтверждением отправлено на " + emailFromIntent,
                    R.drawable.ic_galohca_black,
                    Color.GREEN,
                    Color.BLACK,
                    Color.BLACK);
            mail.setText(emailFromIntent);
        }

        // Инициализация сервисов
        passwordValidationService = new PasswordValidationService();
        sessionManager = new SessionManager(this);
        authManager = new AuthManager();

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
            String email = mail.getText().toString().trim();
            String password = pass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                ToastManager.showToast(this,
                        "Введите логин и пароль!",
                        R.drawable.ic_error,
                        Color.RED,
                        Color.BLACK,
                        Color.BLACK);
                return;
            }

            // Вход пользователя
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // Email подтвержден, разрешаем вход
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("ACCOUNT_MAIL", email);

                                    // Сохраняем хэш пароля
                                    String hashedPassword = passwordValidationService.hashPassword(password);
                                    sessionManager.savePasswordHash(hashedPassword);
                                    passwordValidationService.saveLocalPasswordHash(password);

                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Email не подтвержден
                                    auth.signOut();

                                    // Показываем диалог с предложением отправить письмо повторно
                                    CustomDialogHelper.showSimpleDialog(
                                            this,
                                            "Email не подтвержден",
                                            "Ваш email не подтвержден. Хотите получить письмо с подтверждением повторно?",
                                            "Да",
                                            Color.parseColor("#727272"),
                                            (dialog, which) -> {
                                                // Повторная отправка письма подтверждения
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(verificationTask -> {
                                                            if (verificationTask.isSuccessful()) {
                                                                ToastManager.showToast(this,
                                                                        "Письмо подтверждения отправлено на " + email,
                                                                        R.drawable.ic_galohca_black,
                                                                        Color.GREEN,
                                                                        Color.BLACK,
                                                                        Color.BLACK);
                                                            } else {
                                                                ToastManager.showToast(this,
                                                                        "Ошибка отправки письма",
                                                                        R.drawable.ic_error,
                                                                        Color.RED,
                                                                        Color.BLACK,
                                                                        Color.BLACK);
                                                            }
                                                        });
                                            },
                                            "Отмена",
                                            Color.RED,
                                            (dialog, which) -> dialog.dismiss()
                                    );
                                }
                            }
                        } else {
                            ToastManager.showToast(this,
                                    "Неверный логин или пароль!",
                                    R.drawable.ic_error,
                                    Color.RED,
                                    Color.BLACK,
                                    Color.BLACK);
                        }
                    });
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
                        // Проверяем, существует ли пользователь в базе данных
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = usersRef.orderByChild("email").equalTo(user.getEmail());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    // Если пользователь новый, создаем запись в базе данных
                                    User newUser = new User(user.getEmail(), "github");
                                    usersRef.push().setValue(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                ToastManager.showToast(LoginActivity.this, "Регистрация успешна!", R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);
                                                navigateToMainActivity(user.getEmail());
                                            })
                                            .addOnFailureListener(e -> {
                                                ToastManager.showToast(LoginActivity.this, "Ошибка при создании профиля: " + e.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                                            });
                                } else {
                                    // Если пользователь уже существует, просто переходим в главную активность
                                    navigateToMainActivity(user.getEmail());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                ToastManager.showToast(LoginActivity.this,
                                        "Ошибка проверки пользователя: " + databaseError.getMessage(),
                                        R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGITHUBAUTH", e.getMessage());
                    ToastManager.showToast(this, "Ошибка аутентификации: " + e.getMessage(),
                            R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
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
                        // Проверяем, существует ли пользователь в базе данных
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = usersRef.orderByChild("email").equalTo(user.getEmail());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    // Если пользователь новый, показываем диалог с условиями
                                    showPrivacyTermsDialogForService(user, "google");
                                } else {
                                    // Если пользователь уже существует, просто переходим в главную активность
                                    navigateToMainActivity(user.getEmail());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                ToastManager.showToast(LoginActivity.this,
                                        "Ошибка проверки пользователя: " + databaseError.getMessage(),
                                        R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGOOGLEAUTH", e.getMessage());
                    ToastManager.showToast(this, "Ошибка аутентификации: " + e.getMessage(),
                            R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                });
    }

    private void showPrivacyTermsDialogForService(FirebaseUser user, String service) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_privacy_terms, null);

        TextView privacyPolicyLink = dialogView.findViewById(R.id.privacy_policy_link);
        TextView termsLink = dialogView.findViewById(R.id.terms_link);
        CheckBox acceptCheckbox = dialogView.findViewById(R.id.accept_checkbox);
        Button continueButton = dialogView.findViewById(R.id.continue_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        continueButton.setAlpha(0.5f);

        // Обработка клика по ссылке политики конфиденциальности
        privacyPolicyLink.setOnClickListener(v -> {
            String url = "https://your-website.com/privacy-policy"; // Замените на ваш URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // Обработка клика по ссылке условий использования
        termsLink.setOnClickListener(v -> {
            String url = "https://your-website.com/terms-of-service"; // Замените на ваш URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogStyle);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        acceptCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            continueButton.setEnabled(isChecked);
            continueButton.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        continueButton.setOnClickListener(v -> {
            dialog.dismiss();
            proceedWithServiceLogin(user, service);
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            // Отменяем вход и выходим из аккаунта
            FirebaseAuth.getInstance().signOut();
            ToastManager.showToast(this, "Для использования приложения необходимо принять условия использования", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
        });

        dialog.show();
    }

    private void proceedWithServiceLogin(FirebaseUser user, String service) {
        // Проверяем, существует ли пользователь в базе данных
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = usersRef.orderByChild("email").equalTo(user.getEmail());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Если пользователь новый, создаем запись в базе данных
                    User newUser = new User(user.getEmail(), service);
                    usersRef.push().setValue(newUser)
                            .addOnSuccessListener(aVoid -> {
                                ToastManager.showToast(LoginActivity.this, "Регистрация успешна!", R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);
                                navigateToMainActivity(user.getEmail());
                            })
                            .addOnFailureListener(e -> {
                                ToastManager.showToast(LoginActivity.this, "Ошибка при создании профиля: " + e.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                            });
                } else {
                    // Если пользователь уже существует, просто переходим в главную активность
                    navigateToMainActivity(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastManager.showToast(LoginActivity.this, "Ошибка проверки пользователя: " + databaseError.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
            }
        });
    }

    private void navigateToMainActivity(String email) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ACCOUNT_LOGIN", false);
        intent.putExtra("ACCOUNT_MAIL", email);
        startActivity(intent);
        finish();
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

    private void showVerificationDialog() {
        CustomDialogHelper.showSimpleDialog(
                this,
                "Подтверждение email",
                "Ваш email не подтвержден. Хотите получить письмо с подтверждением повторно?",
                "Да",
                Color.parseColor("#727272"),
                (dialog, which) -> {
                    authManager.resendVerificationEmail(new AuthManager.OnRegistrationListener() {
                        @Override
                        public void onSuccess(String message) {
                            dialog.dismiss();
                            ToastManager.showToast(LoginActivity.this, message,
                                    R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);
                        }

                        @Override
                        public void onFailure(String error) {
                            dialog.dismiss();
                            ToastManager.showToast(LoginActivity.this, error,
                                    R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                        }
                    });
                },
                "Отмена",
                Color.RED,
                (dialog, which) -> dialog.dismiss()
        );
    }

}
