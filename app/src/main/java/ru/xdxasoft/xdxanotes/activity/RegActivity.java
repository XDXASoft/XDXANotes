package ru.xdxasoft.xdxanotes.activity;

import android.content.Context;
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
import androidx.core.content.ContextCompat;

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
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applyLanguage(this);
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
                signInWithVK();
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
                ToastManager.showToast(this,
                        getString(R.string.Enter_your_login_and_password),
                        R.drawable.ic_error,
                        ContextCompat.getColor(this, R.color.error_red),
                        ContextCompat.getColor(this, R.color.black),
                        ContextCompat.getColor(this, R.color.black));
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
                    ToastManager.showToast(
                            RegActivity.this,
                            getString(R.string.To_use_the_application_you_must_accept_the_terms_of_use),
                            R.drawable.ic_galohca_black,
                            ContextCompat.getColor(RegActivity.this, R.color.success_green),
                            ContextCompat.getColor(RegActivity.this, R.color.black),
                            ContextCompat.getColor(RegActivity.this, R.color.black)
                    );
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
                        ContextCompat.getColor(RegActivity.this, R.color.error_red),
                        ContextCompat.getColor(RegActivity.this, R.color.black),
                        ContextCompat.getColor(RegActivity.this, R.color.black)
                );
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        regbtn.setEnabled(!show);
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
                                ToastManager.showToast(RegActivity.this,
                                        getString(R.string.User_verification_error) + databaseError.getMessage(),
                                        R.drawable.ic_error,
                                        ContextCompat.getColor(RegActivity.this, R.color.error_red),
                                        ContextCompat.getColor(RegActivity.this, R.color.black),
                                        ContextCompat.getColor(RegActivity.this, R.color.black));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGITHUBAUTH", e.getMessage());
                    ToastManager.showToast(this,
                            getString(R.string.Authentication_error) + e.getMessage(),
                            R.drawable.ic_error,
                            ContextCompat.getColor(this, R.color.error_red),
                            ContextCompat.getColor(this, R.color.black),
                            ContextCompat.getColor(this, R.color.black));
                });
    }

    private void signInWithGoogle() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("google.com");

        List<String> scopes = new ArrayList<>();
        scopes.add("email");

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
                                ToastManager.showToast(RegActivity.this,
                                        getString(R.string.User_verification_error) + databaseError.getMessage(),
                                        R.drawable.ic_error,
                                        ContextCompat.getColor(RegActivity.this, R.color.error_red),
                                        ContextCompat.getColor(RegActivity.this, R.color.black),
                                        ContextCompat.getColor(RegActivity.this, R.color.black));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERRGOOGLEAUTH", e.getMessage());
                    ToastManager.showToast(this,
                            getString(R.string.Authentication_error) + e.getMessage(),
                            R.drawable.ic_error,
                            ContextCompat.getColor(this, R.color.error_red),
                            ContextCompat.getColor(this, R.color.black),
                            ContextCompat.getColor(this, R.color.black));
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
                                    ToastManager.showToast(RegActivity.this,
                                            getString(R.string.Registration_successful),
                                            R.drawable.ic_galohca_black,
                                            ContextCompat.getColor(RegActivity.this, R.color.success_green),
                                            ContextCompat.getColor(RegActivity.this, R.color.black),
                                            ContextCompat.getColor(RegActivity.this, R.color.black));
                                    navigateToMainActivity(user.getEmail(), true);
                                })
                                .addOnFailureListener(e -> {
                                    ToastManager.showToast(RegActivity.this,
                                            getString(R.string.Error_creating_profile) + e.getMessage(),
                                            R.drawable.ic_error,
                                            ContextCompat.getColor(RegActivity.this, R.color.error_red),
                                            ContextCompat.getColor(RegActivity.this, R.color.black),
                                            ContextCompat.getColor(RegActivity.this, R.color.black));
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
                    ToastManager.showToast(RegActivity.this,
                            getString(R.string.User_verification_error) + databaseError.getMessage(),
                            R.drawable.ic_error,
                            ContextCompat.getColor(RegActivity.this, R.color.error_red),
                            ContextCompat.getColor(RegActivity.this, R.color.black),
                            ContextCompat.getColor(RegActivity.this, R.color.black));
                }
            });
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut();
                    ToastManager.showToast(RegActivity.this,
                            getString(R.string.Registration_canceled),
                            R.drawable.ic_error,
                            ContextCompat.getColor(RegActivity.this, R.color.error_red),
                            ContextCompat.getColor(RegActivity.this, R.color.black),
                            ContextCompat.getColor(RegActivity.this, R.color.black));
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
        ToastManager.showToast(RegActivity.this,
                getString(R.string.In_development),
                R.drawable.ic_settings,
                ContextCompat.getColor(this, R.color.warning_yellow),
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.black));
    }

}