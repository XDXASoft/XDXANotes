package ru.xdxasoft.xdxanotes.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.LoginActivity;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class PinAuthFragment extends Fragment implements View.OnClickListener {

    private static final String PREFS_NAME = "PinPrefs";
    private static final String PREF_FINGERPRINT_ENABLED = "fingerprint_enabled";
    private static final String PREF_FIRST_LOGIN = "first_login";
    private static final String TAG = "PinAuthFragment";

    private TextView tvPinTitle;
    private TextView tvPinError;
    private View pinDot1, pinDot2, pinDot3, pinDot4;
    private Button[] numButtons = new Button[10];
    private ImageButton btnFingerprint;
    private ImageButton btnBackspace;

    private StringBuilder currentPin = new StringBuilder();
    private String confirmPin = "";
    private boolean createMode = false;

    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private boolean hasVibrationPermission = false;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        btnFingerprint = view.findViewById(R.id.btnKeypadFingerprint);
        btnBackspace = view.findViewById(R.id.btnKeypadBackspace);

        hasVibrationPermission = ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED;

        initViews(view);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLoginActivity();
            return;
        }

        checkPinInFirebase(currentUser.getUid());

        if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            setupBiometricAuth();
            updateButtonVisibility();
        } else {
            btnFingerprint.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        tvPinTitle = view.findViewById(R.id.tvPinTitle);
        tvPinError = view.findViewById(R.id.tvPinError);

        pinDot1 = view.findViewById(R.id.pinDot1);
        pinDot2 = view.findViewById(R.id.pinDot2);
        pinDot3 = view.findViewById(R.id.pinDot3);
        pinDot4 = view.findViewById(R.id.pinDot4);

        numButtons[0] = view.findViewById(R.id.btnKeypad0);
        numButtons[1] = view.findViewById(R.id.btnKeypad1);
        numButtons[2] = view.findViewById(R.id.btnKeypad2);
        numButtons[3] = view.findViewById(R.id.btnKeypad3);
        numButtons[4] = view.findViewById(R.id.btnKeypad4);
        numButtons[5] = view.findViewById(R.id.btnKeypad5);
        numButtons[6] = view.findViewById(R.id.btnKeypad6);
        numButtons[7] = view.findViewById(R.id.btnKeypad7);
        numButtons[8] = view.findViewById(R.id.btnKeypad8);
        numButtons[9] = view.findViewById(R.id.btnKeypad9);

        for (Button button : numButtons) {
            button.setOnClickListener(this);
        }
        btnBackspace.setOnClickListener(this);
        btnFingerprint.setOnClickListener(this);
    }

    private void updateButtonVisibility() {
        if (currentPin.length() == 0 && !createMode && isFingerprintEnabled()) {
            btnFingerprint.setVisibility(View.VISIBLE);
        } else {
            btnFingerprint.setVisibility(View.GONE);
        }
        btnBackspace.setVisibility(currentPin.length() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        for (int i = 0; i < numButtons.length; i++) {
            if (id == numButtons[i].getId()) {
                addDigit(String.valueOf(i));
                return;
            }
        }

        if (id == R.id.btnKeypadBackspace) {
            removeDigit();
        } else if (id == R.id.btnKeypadFingerprint) {
            if (!createMode && isFingerprintEnabled() && currentPin.length() == 0) {
                showBiometricPrompt();
            }
        }
    }

    private void addDigit(String digit) {
        if (currentPin.length() < 4) {
            vibrate();
            currentPin.append(digit);
            updatePinDots();
            updateButtonVisibility();

            if (currentPin.length() == 4) {
                new Handler().postDelayed(this::processPinEntry, 200);
            }
        }
    }

    private void removeDigit() {
        if (currentPin.length() > 0) {
            vibrate();
            currentPin.deleteCharAt(currentPin.length() - 1);
            updatePinDots();
            updateButtonVisibility();
            tvPinError.setVisibility(View.INVISIBLE);
        }
    }

    private void vibrate() {
        if (vibrator != null && hasVibrationPermission) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(20);
                }
            } catch (Exception e) {
                Log.e(TAG, "Vibration failed", e);
            }
        }
    }

    private void updatePinDots() {
        pinDot1.setBackgroundResource(currentPin.length() >= 1 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        pinDot2.setBackgroundResource(currentPin.length() >= 2 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        pinDot3.setBackgroundResource(currentPin.length() >= 3 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        pinDot4.setBackgroundResource(currentPin.length() >= 4 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
    }

    private void updateUI() {
        if (createMode) {
            if (confirmPin.isEmpty()) {
                tvPinTitle.setText(R.string.pin_create);
            } else {
                tvPinTitle.setText(R.string.pin_confirm);
            }
        } else {
            tvPinTitle.setText(R.string.pin_enter);
        }
    }

    private void processPinEntry() {
        String enteredPin = currentPin.toString();

        if (createMode) {
            if (confirmPin.isEmpty()) {
                confirmPin = enteredPin;
                resetPinEntry();
                updateUI();
            } else {
                if (enteredPin.equals(confirmPin)) {
                    savePinToFirebase(enteredPin);
                    ToastManager.showToast(requireContext(), getString(R.string.pin_success),
                            R.drawable.ic_galohca_black,
                            ContextCompat.getColor(requireContext(), R.color.success_green),
                            ContextCompat.getColor(requireContext(), R.color.black),
                            ContextCompat.getColor(requireContext(), R.color.black));
                    navigateToPasswordFragment();
                } else {
                    showError(getString(R.string.pin_not_match));
                    confirmPin = "";
                    resetPinEntry();
                    updateUI();
                }
            }
        } else {
            verifyPinInFirebase(enteredPin);
        }
    }

    private void resetPinEntry() {
        currentPin.setLength(0);
        updatePinDots();
        updateButtonVisibility();
    }

    private void showError(String message) {
        tvPinError.setText(message);
        tvPinError.setVisibility(View.VISIBLE);

        if (vibrator != null && hasVibrationPermission) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 100, 50, 100}, -1));
                } else {
                    vibrator.vibrate(new long[]{0, 100, 50, 100}, -1);
                }
            } catch (Exception e) {
                Log.e(TAG, "Vibration failed", e);
            }
        }
    }

    private void checkPinInFirebase(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("pin_hash");
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                createMode = false;
            } else {
                createMode = true;
                btnFingerprint.setVisibility(View.GONE);
            }
            updateUI();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Ошибка проверки пин-кода в Firebase: " + e.getMessage());
            ToastManager.showToast(requireContext(), getString(R.string.Network_error_please_try_again_later), R.drawable.ic_error_black,
                    ContextCompat.getColor(requireContext(), R.color.error_red),
                    ContextCompat.getColor(requireContext(), R.color.black),
                    ContextCompat.getColor(requireContext(), R.color.black));
            navigateToLoginActivity();
        });
    }

    private void savePinToFirebase(String pin) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Пользователь не авторизован при сохранении пин-кода");
            navigateToLoginActivity();
            return;
        }

        String pinHash = hashPin(pin);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("pin_hash");
        userRef.setValue(pinHash).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Пин-код успешно сохранен в Firebase");
                sharedPreferences.edit()
                        .putBoolean(PREF_FIRST_LOGIN, false)
                        .apply();
            } else {
                Log.e(TAG, "Ошибка сохранения пин-кода в Firebase: " + task.getException().getMessage());
                showError(getString(R.string.Error_saving_pin_code));
            }
        });
    }

    private void verifyPinInFirebase(String pin) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Пользователь не авторизован при проверке пин-кода");
            navigateToLoginActivity();
            return;
        }

        String inputHash = hashPin(pin);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("pin_hash");
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String storedHash = task.getResult().getValue(String.class);
                if (storedHash != null && storedHash.equals(inputHash)) {
                    navigateToPasswordFragment();
                } else {
                    showError(getString(R.string.pin_incorrect));
                    resetPinEntry();
                }
            } else {
                Log.e(TAG, "Ошибка проверки пин-кода: " + task.getException().getMessage());
                showError(getString(R.string.Network_error));
                resetPinEntry();
            }
        });
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Hashing failed", e);
            return pin;
        }
    }

    private boolean isFingerprintEnabled() {
        return sharedPreferences.getBoolean(PREF_FINGERPRINT_ENABLED, false);
    }

    private void setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        navigateToPasswordFragment();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED
                                && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            showError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        showError(getString(R.string.wrong_password));
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_login))
                .setSubtitle(getString(R.string.login_using_biometric_credential))
                .setNegativeButtonText(getString(R.string.use_password))
                .build();
    }

    private void showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo);
    }

    private void navigateToPasswordFragment() {
        if (createMode && BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            sharedPreferences.edit()
                    .putBoolean(PREF_FINGERPRINT_ENABLED, true)
                    .apply();
        }

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentFragment, new PasswordFragment());
        transaction.commit();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}