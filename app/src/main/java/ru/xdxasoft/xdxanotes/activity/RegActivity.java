package ru.xdxasoft.xdxanotes.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;
import ru.xdxasoft.xdxanotes.utils.AuthManager;

public class RegActivity extends AppCompatActivity {

    private Button regbtn;

    private ImageButton github_button, google_button, vk_button;
    private EditText mailreg, passreg;
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
        mauth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
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

            }
        });

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

            if (email.isEmpty() || password.isEmpty()) {
                ToastManager.showToast(this, "Введите логин и пароль!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
            } else {
                showPrivacyTermsDialog(email, password);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mailreg.getWindowToken(), 0);
        }
    }

    private void showPrivacyTermsDialog(String email, String password) {
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
            proceedWithRegistration(email, password);
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void proceedWithRegistration(String email, String password) {
        showLoading(true);
        authManager.registerUser(email, password, new AuthManager.OnRegistrationListener() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    showPrivacyTermsDialogForService(user, "email");
                }
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

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        regbtn.setEnabled(!show);
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void LoginActivity(View v) {
        Intent intent = new Intent(RegActivity.this, LoginActivity.class);
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
                                    // Если пользователь новый, показываем диалог с условиями
                                    showPrivacyTermsDialogForService(user, "github");
                                } else {
                                    // Если пользователь уже существует, просто переходим в главную активность
                                    navigateToMainActivity(user.getEmail());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                ToastManager.showToast(RegActivity.this,
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
                                ToastManager.showToast(RegActivity.this,
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
            // Создаем запись в базе данных
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            User newUser = new User(user.getEmail(), service);
            usersRef.push().setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        ToastManager.showToast(RegActivity.this,
                                "Регистрация успешна!",
                                R.drawable.ic_galohca_black,
                                Color.GREEN,
                                Color.BLACK,
                                Color.BLACK);
                        navigateToMainActivity(user.getEmail());
                    })
                    .addOnFailureListener(e -> {
                        ToastManager.showToast(RegActivity.this,
                                "Ошибка при создании профиля: " + e.getMessage(),
                                R.drawable.ic_error,
                                Color.RED,
                                Color.BLACK,
                                Color.BLACK);
                    });
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            // Отменяем регистрацию
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ToastManager.showToast(RegActivity.this,
                            "Регистрация отменена",
                            R.drawable.ic_error,
                            Color.RED,
                            Color.BLACK,
                            Color.BLACK);
                }
            });
        });

        dialog.show();
    }

    private void navigateToMainActivity(String email) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ACCOUNT_LOGIN", false);
        intent.putExtra("ACCOUNT_MAIL", email);
        startActivity(intent);
        finish();
    }

}
