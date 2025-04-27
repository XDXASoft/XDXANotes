package ru.xdxasoft.xdxanotes.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.concurrent.Executor;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.services.PasswordValidationService;

public class PasswordAuthFragment extends Fragment {

    // Constants for shared preferences
    private static final String PREFS_NAME = "PasswordPrefs";
    private static final String PREF_PASSWORD_HASH = "password_hash";
    private static final String PREF_FINGERPRINT_ENABLED = "fingerprint_enabled";
    private static final String PREF_FIRST_LOGIN = "first_login";

    // UI elements
    private TextView tvTitle;
    private TextView tvDescription;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSubmit;
    private LinearLayout fingerprintContainer;
    private Button btnUseFingerprintAuth;

    // Services
    private PasswordValidationService passwordService;
    private SharedPreferences sharedPreferences;

    // Biometric authentication
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services
        passwordService = new PasswordValidationService();
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize UI elements
        initViews(view);
        setupUI();

        // Set up biometric authentication if available
        if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            setupBiometricAuth();
        }
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        fingerprintContainer = view.findViewById(R.id.fingerprintContainer);
        btnUseFingerprintAuth = view.findViewById(R.id.btnUseFingerprintAuth);
    }

    private void setupUI() {
        String passwordHash = sharedPreferences.getString(PREF_PASSWORD_HASH, null);
        boolean isFirstLogin = sharedPreferences.getBoolean(PREF_FIRST_LOGIN, true);
        boolean isFingerprintEnabled = sharedPreferences.getBoolean(PREF_FINGERPRINT_ENABLED, false);

        if (passwordHash == null) {
            // Password not set yet, show create password UI
            setupCreatePasswordUI();
        } else {
            // Password exists, show login UI
            setupLoginUI(isFingerprintEnabled);

            // If it's the first login on this device and fingerprint is available,
            // we'll suggest enabling it after successful password verification
            if (isFirstLogin && BiometricManager.from(requireContext())
                    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    == BiometricManager.BIOMETRIC_SUCCESS) {
                sharedPreferences.edit().putBoolean(PREF_FIRST_LOGIN, false).apply();
            }
        }
    }

    private void setupCreatePasswordUI() {
        tvTitle.setText(R.string.create_password);
        tvDescription.setText(R.string.create_password_description);
        etConfirmPassword.setVisibility(View.VISIBLE);
        btnSubmit.setText(R.string.create);

        btnSubmit.setOnClickListener(v -> createPassword());
    }

    private void setupLoginUI(boolean isFingerprintEnabled) {
        tvTitle.setText(R.string.enter_password);
        tvDescription.setText(R.string.enter_password_description);
        etConfirmPassword.setVisibility(View.GONE);
        btnSubmit.setText(R.string.login);

        btnSubmit.setOnClickListener(v -> verifyPassword());

        // Show fingerprint button if enabled
        if (isFingerprintEnabled) {
            fingerprintContainer.setVisibility(View.VISIBLE);
            btnUseFingerprintAuth.setOnClickListener(v -> showBiometricPrompt());
        } else {
            fingerprintContainer.setVisibility(View.GONE);
        }
    }

    private void createPassword() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), R.string.empty_password_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), R.string.password_mismatch_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash and save the password
        String passwordHash = passwordService.hashPassword(password);
        sharedPreferences.edit()
                .putString(PREF_PASSWORD_HASH, passwordHash)
                .putBoolean(PREF_FIRST_LOGIN, false)
                .apply();

        // Check if biometric authentication is available
        if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            // Ask if user wants to enable fingerprint authentication
            showEnableFingerprintDialog();
        } else {
            // Navigate to password fragment
            navigateToPasswordFragment();
        }
    }

    private void verifyPassword() {
        String password = etPassword.getText().toString();
        String savedPasswordHash = sharedPreferences.getString(PREF_PASSWORD_HASH, "");

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), R.string.empty_password_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String inputPasswordHash = passwordService.hashPassword(password);

        if (savedPasswordHash.equals(inputPasswordHash)) {
            navigateToPasswordFragment();
        } else {
            Toast.makeText(requireContext(), R.string.invalid_password, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Authentication succeeded, navigate to password fragment
                navigateToPasswordFragment();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // If it's not a user cancellation, show error
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED
                        && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(requireContext(), errString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_login_title))
                .setSubtitle(getString(R.string.biometric_login_subtitle))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
    }

    private void showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo);
    }

    private void showEnableFingerprintDialog() {
        // Show dialog asking if user wants to enable fingerprint authentication
        // If yes, enable it in shared preferences
        sharedPreferences.edit().putBoolean(PREF_FINGERPRINT_ENABLED, true).apply();
        navigateToPasswordFragment();
    }

    private void navigateToPasswordFragment() {
        // Navigate to the PasswordFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.contentFragment, new PasswordFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
