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
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.services.PasswordValidationService;
import ru.xdxasoft.xdxanotes.utils.AuthManager;
import ru.xdxasoft.xdxanotes.utils.CustomDialogHelper;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LocaleHelper.applyLanguage(this);

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        boolean isVerificationSent = getIntent().getBooleanExtra("ACCOUNT_VERI", false);
        String emailFromIntent = getIntent().getStringExtra("ACCOUNT_MAIL");
        boolean privacyNotAccepted = getIntent().getBooleanExtra("PRIVACY_NOT_ACCEPTED", false);

        if (privacyNotAccepted) {
            ToastManager.showToast(this,
                    getString(R.string.To_use_the_application_you_must_accept_the_privacy_policy),
                    R.drawable.ic_error,
                    ContextCompat.getColor(this, R.color.error_red),
                    ContextCompat.getColor(this, R.color.black),
                    ContextCompat.getColor(this, R.color.black));
        } else if (isVerificationSent && emailFromIntent != null) {
            ToastManager.showToast(this,
                    getString(R.string.Confirmation_email_sent_to) + emailFromIntent,
                    R.drawable.ic_galohca_black,
                    ContextCompat.getColor(this, R.color.success_green),
                    ContextCompat.getColor(this, R.color.black),
                    ContextCompat.getColor(this, R.color.black));
            mail.setText(emailFromIntent);
        }

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
                signInWithVK();
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
                        getString(R.string.Enter_your_login_and_password),
                        R.drawable.ic_error,
                        ContextCompat.getColor(this, R.color.error_red),
                        ContextCompat.getColor(this, R.color.black),
                        ContextCompat.getColor(this, R.color.black));
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("ACCOUNT_MAIL", email);

                                    String hashedPassword = passwordValidationService.hashPassword(password);
                                    sessionManager.savePasswordHash(hashedPassword);
                                    passwordValidationService.saveLocalPasswordHash(password);

                                    startActivity(intent);
                                    finish();
                                } else {
                                    auth.signOut();

                                    showVerificationDialog(user, email);
                                }
                            }
                        } else {
                            ToastManager.showToast(this,
                                    getString(R.string.Incorrect_login_or_password),
                                    R.drawable.ic_error,
                                    ContextCompat.getColor(this, R.color.error_red),
                                    ContextCompat.getColor(this, R.color.black),
                                    ContextCompat.getColor(this, R.color.black));
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
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = usersRef.orderByChild("email").equalTo(user.getEmail());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    boolean privacyAccepted = false;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User existingUser = snapshot.getValue(User.class);
                                        if (existingUser != null && existingUser.isPrivacyAccepted()) {
                                            privacyAccepted = true;
                                            break;
                                        }
                                    }

                                    if (privacyAccepted) {
                                        navigateToMainActivity(user.getEmail(), true);
                                    } else {
                                        showPrivacyTermsDialogForService(user, "github");
                                    }
                                } else {
                                    showPrivacyTermsDialogForService(user, "github");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("GITHUB_AUTH", "Ошибка проверки пользователя: " + databaseError.getMessage());
                                ToastManager.showToast(LoginActivity.this,
                                        getString(R.string.User_verification_error) + databaseError.getMessage(),
                                        R.drawable.ic_error,
                                        ContextCompat.getColor(LoginActivity.this, R.color.error_red),
                                        ContextCompat.getColor(LoginActivity.this, R.color.black),
                                        ContextCompat.getColor(LoginActivity.this, R.color.black));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGITHUBAUTH", e.getMessage());
                    ToastManager.showToast(this, getString(R.string.Authentication_error) + e.getMessage(),
                            R.drawable.ic_error, ContextCompat.getColor(this, R.color.error_red), ContextCompat.getColor(this, R.color.black), ContextCompat.getColor(this, R.color.black));
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
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = usersRef.orderByChild("email").equalTo(user.getEmail());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    boolean privacyAccepted = false;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User existingUser = snapshot.getValue(User.class);
                                        if (existingUser != null && existingUser.isPrivacyAccepted()) {
                                            privacyAccepted = true;
                                            break;
                                        }
                                    }

                                    if (privacyAccepted) {
                                        navigateToMainActivity(user.getEmail(), true);
                                    } else {
                                        showPrivacyTermsDialogForService(user, "google");
                                    }
                                } else {

                                    showPrivacyTermsDialogForService(user, "google");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("GOOGLE_AUTH", "Ошибка проверки пользователя: " + databaseError.getMessage());
                                ToastManager.showToast(LoginActivity.this,
                                        getString(R.string.User_verification_error) + databaseError.getMessage(),
                                        R.drawable.ic_error,
                                        ContextCompat.getColor(LoginActivity.this, R.color.error_red),
                                        ContextCompat.getColor(LoginActivity.this, R.color.black),
                                        ContextCompat.getColor(LoginActivity.this, R.color.black));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGOOGLEAUTH", e.getMessage());
                    ToastManager.showToast(this, getString(R.string.Authentication_error) + e.getMessage(),
                            R.drawable.ic_error, ContextCompat.getColor(this, R.color.error_red), ContextCompat.getColor(this, R.color.black), ContextCompat.getColor(this, R.color.black));
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
        continueButton.setEnabled(false);

        privacyPolicyLink.setOnClickListener(v -> {
            String url = "https://notes.xdxa.ru/privacy-policy";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        termsLink.setOnClickListener(v -> {
            String url = "https://notes.xdxa.ru/terms-of-service";
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
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            Query query = usersRef.orderByChild("email").equalTo(user.getEmail());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        User newUser = new User(user.getEmail(), service, true);
                        usersRef.push().setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    ToastManager.showToast(LoginActivity.this,
                                            getString(R.string.Registration_successful),
                                            R.drawable.ic_galohca_black,
                                            ContextCompat.getColor(LoginActivity.this, R.color.success_green),
                                            ContextCompat.getColor(LoginActivity.this, R.color.black),
                                            ContextCompat.getColor(LoginActivity.this, R.color.black));
                                    navigateToMainActivity(user.getEmail(), true);
                                })
                                .addOnFailureListener(e -> {
                                    ToastManager.showToast(LoginActivity.this,
                                            getString(R.string.Error_creating_profile) + e.getMessage(),
                                            R.drawable.ic_error,
                                            ContextCompat.getColor(LoginActivity.this, R.color.error_red),
                                            ContextCompat.getColor(LoginActivity.this, R.color.black),
                                            ContextCompat.getColor(LoginActivity.this, R.color.black));
                                });
                    } else {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userKey = snapshot.getKey();
                            if (userKey != null) {
                                usersRef.child(userKey).child("privacyAccepted").setValue(true);
                            }
                        }
                        navigateToMainActivity(user.getEmail(), true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    ToastManager.showToast(LoginActivity.this,
                            getString(R.string.User_verification_error) + databaseError.getMessage(),
                            R.drawable.ic_error,
                            ContextCompat.getColor(LoginActivity.this, R.color.error_red),
                            ContextCompat.getColor(LoginActivity.this, R.color.black),
                            ContextCompat.getColor(LoginActivity.this, R.color.black));
                }
            });
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut();
                    ToastManager.showToast(this,
                            getString(R.string.Registration_canceled),
                            R.drawable.ic_error,
                            ContextCompat.getColor(this, R.color.error_red),
                            ContextCompat.getColor(this, R.color.black),
                            ContextCompat.getColor(this, R.color.black));
                }
            });
        });

        dialog.show();
    }

    private void navigateToMainActivity(String email, boolean privacyAccepted) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ACCOUNT_LOGIN", false);
        intent.putExtra("ACCOUNT_MAIL", email);
        intent.putExtra("PRIVACY_ACCEPTED", privacyAccepted);
        startActivity(intent);
        finish();
    }

    private void signInWithVK() {
        ToastManager.showToast(LoginActivity.this,
                getString(R.string.In_development),
                R.drawable.ic_settings,
                ContextCompat.getColor(this, R.color.warning_yellow),
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.black));
    }

    private void showVerificationDialog(FirebaseUser user, String email) {
        CustomDialogHelper.showSimpleDialog(
                this,
                getString(R.string.Email_not_confirmed),
                getString(R.string.Your_email_is_not_confirmed_Do_you_want_to_receive_the_confirmation_email_again),
                getString(R.string.Yes),
                ContextCompat.getColor(this, R.color.dialog_neutral_gray),
                (dialog, which) -> {
                    authManager.resendVerificationEmail(new AuthManager.OnRegistrationListener() {
                        @Override
                        public void onSuccess(String message) {
                            ToastManager.showToast(LoginActivity.this,
                                    message,
                                    R.drawable.ic_galohca_black,
                                    ContextCompat.getColor(LoginActivity.this, R.color.success_green),
                                    ContextCompat.getColor(LoginActivity.this, R.color.black),
                                    ContextCompat.getColor(LoginActivity.this, R.color.black));
                        }

                        @Override
                        public void onFailure(String error) {
                            ToastManager.showToast(LoginActivity.this,
                                    error,
                                    R.drawable.ic_error,
                                    ContextCompat.getColor(LoginActivity.this, R.color.error_red),
                                    ContextCompat.getColor(LoginActivity.this, R.color.black),
                                    ContextCompat.getColor(LoginActivity.this, R.color.black));
                        }
                    });
                },
                getString(R.string.Cancel),
                ContextCompat.getColor(this, R.color.error_red),
                (dialog, which) -> dialog.dismiss()
        );
    }

}