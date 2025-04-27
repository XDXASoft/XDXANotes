package ru.xdxasoft.xdxanotes.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class PinAuthFragment extends Fragment implements View.OnClickListener {

    private static final String PREFS_NAME = "PinPrefs";
    private static final String PREF_PIN_HASH = "pin_hash";
    private static final String PREF_FINGERPRINT_ENABLED = "fingerprint_enabled";
    private static final String PREF_FIRST_LOGIN = "first_login";

    // UI elements
    private TextView tvPinTitle;
    private TextView tvPinError;
    private View pinDot1, pinDot2, pinDot3, pinDot4;
    private Button[] numButtons = new Button[10];
    private ImageButton btnFingerprint;
    private ImageButton btnBackspace;

    // Pin storage
    private StringBuilder currentPin = new StringBuilder();
    private String confirmPin = "";
    private boolean createMode = false;

    // Services
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private boolean hasVibrationPermission = false;

    // Biometric authentication
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Check if we have vibration permission
        hasVibrationPermission = ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED;

        // Initialize UI elements
        initViews(view);

        // Set mode based on PIN existence
        createMode = !isPinSet();
        updateUI();

        // Set up biometric authentication if available
        if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            setupBiometricAuth();
            btnFingerprint.setVisibility(View.VISIBLE);
        } else {
            btnFingerprint.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        // Title and error
        tvPinTitle = view.findViewById(R.id.tvPinTitle);
        tvPinError = view.findViewById(R.id.tvPinError);

        // PIN dots
        pinDot1 = view.findViewById(R.id.pinDot1);
        pinDot2 = view.findViewById(R.id.pinDot2);
        pinDot3 = view.findViewById(R.id.pinDot3);
        pinDot4 = view.findViewById(R.id.pinDot4);

        // Numeric buttons
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

        // Special buttons
        btnFingerprint = view.findViewById(R.id.btnKeypadFingerprint);
        btnBackspace = view.findViewById(R.id.btnKeypadBackspace);

        // Set click listeners
        for (Button button : numButtons) {
            button.setOnClickListener(this);
        }
        btnBackspace.setOnClickListener(this);
        btnFingerprint.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Handle number buttons
        for (int i = 0; i < numButtons.length; i++) {
            if (id == numButtons[i].getId()) {
                addDigit(String.valueOf(i));
                return;
            }
        }

        // Handle special buttons
        if (id == R.id.btnKeypadBackspace) {
            removeDigit();
        } else if (id == R.id.btnKeypadFingerprint) {
            if (isPinSet() && isFingerprintEnabled() && currentPin.length() == 0) {
                showBiometricPrompt();
            }
        }
    }

    private void addDigit(String digit) {
        if (currentPin.length() < 4) {
            // Vibrate on button press - only if permission is granted
            if (vibrator != null && hasVibrationPermission) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // Deprecated in API 26
                        vibrator.vibrate(20);
                    }
                } catch (Exception e) {
                    // Fail silently
                }
            }

            // Add digit to current PIN
            currentPin.append(digit);

            // Update PIN dots
            updatePinDots();

            // Check if PIN is complete (4 digits)
            if (currentPin.length() == 4) {
                Handler handler = new Handler();
                handler.postDelayed(this::processPinEntry, 200);
            }
        }
    }

    private void removeDigit() {
        if (currentPin.length() > 0) {
            // Vibrate on button press - only if permission is granted
            if (vibrator != null && hasVibrationPermission) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // Deprecated in API 26
                        vibrator.vibrate(20);
                    }
                } catch (Exception e) {
                    // Fail silently
                }
            }

            // Remove last digit
            currentPin.deleteCharAt(currentPin.length() - 1);

            // Update PIN dots
            updatePinDots();

            // Clear error message when editing
            tvPinError.setVisibility(View.INVISIBLE);
        }
    }

    private void updatePinDots() {
        // Update the PIN dots based on current entry
        pinDot1.setBackgroundResource(currentPin.length() >= 1 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        pinDot2.setBackgroundResource(currentPin.length() >= 2 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        pinDot3.setBackgroundResource(currentPin.length() >= 3 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        pinDot4.setBackgroundResource(currentPin.length() >= 4 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);

        // Show/hide fingerprint button based on PIN entry
        if (isPinSet() && isFingerprintEnabled()) {
            btnFingerprint.setVisibility(currentPin.length() == 0 ? View.VISIBLE : View.INVISIBLE);
            btnBackspace.setVisibility(currentPin.length() > 0 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void updateUI() {
        // Update UI based on current mode (create or verify)
        if (createMode) {
            if (confirmPin.isEmpty()) {
                // First PIN entry in create mode
                tvPinTitle.setText(R.string.pin_create);
            } else {
                // Confirming PIN in create mode
                tvPinTitle.setText(R.string.pin_confirm);
            }
        } else {
            // Verify mode
            tvPinTitle.setText(R.string.pin_enter);
        }
    }

    private void processPinEntry() {
        String enteredPin = currentPin.toString();

        if (createMode) {
            if (confirmPin.isEmpty()) {
                // First PIN entry in create mode
                confirmPin = enteredPin;
                resetPinEntry();
                updateUI();
            } else {
                // Confirming PIN in create mode
                if (enteredPin.equals(confirmPin)) {
                    // PINs match, save the PIN
                    savePin(enteredPin);
                    ToastManager.showToast(requireContext(), getString(R.string.pin_success),
                            R.drawable.ic_galohca_black,
                            ContextCompat.getColor(requireContext(), R.color.success_green),
                            ContextCompat.getColor(requireContext(), R.color.black),
                            ContextCompat.getColor(requireContext(), R.color.black));
                    navigateToPasswordFragment();
                } else {
                    // PINs don't match
                    showError(getString(R.string.pin_not_match));
                    confirmPin = "";
                    resetPinEntry();
                    updateUI();
                }
            }
        } else {
            // Verify mode
            if (verifyPin(enteredPin)) {
                // PIN is correct
                navigateToPasswordFragment();
            } else {
                // PIN is incorrect
                showError(getString(R.string.pin_incorrect));
                resetPinEntry();
            }
        }
    }

    private void resetPinEntry() {
        currentPin.setLength(0);
        updatePinDots();
    }

    private void showError(String message) {
        tvPinError.setText(message);
        tvPinError.setVisibility(View.VISIBLE);

        // Vibrate on error - only if permission is granted
        if (vibrator != null && hasVibrationPermission) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 100, 50, 100}, -1));
                } else {
                    // Deprecated in API 26
                    vibrator.vibrate(new long[]{0, 100, 50, 100}, -1);
                }
            } catch (Exception e) {
                // Fail silently
            }
        }
    }

    private boolean isPinSet() {
        return sharedPreferences.contains(PREF_PIN_HASH);
    }

    private boolean isFingerprintEnabled() {
        return sharedPreferences.getBoolean(PREF_FINGERPRINT_ENABLED, false);
    }

    private void savePin(String pin) {
        String pinHash = hashPin(pin);
        sharedPreferences.edit()
                .putString(PREF_PIN_HASH, pinHash)
                .putBoolean(PREF_FIRST_LOGIN, false)
                .apply();
    }

    private boolean verifyPin(String pin) {
        String storedHash = sharedPreferences.getString(PREF_PIN_HASH, "");
        String inputHash = hashPin(pin);
        return storedHash.equals(inputHash);
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
            e.printStackTrace();
            return pin; // Fallback to plain text
        }
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

    private void showEnableFingerprintDialog() {
        // After successful PIN creation, ask if user wants to enable fingerprint authentication
        if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            // Set flag for fingerprint authentication
            sharedPreferences.edit()
                    .putBoolean(PREF_FINGERPRINT_ENABLED, true)
                    .apply();
        }
    }

    private void navigateToPasswordFragment() {
        // If this is first login after creating PIN, ask about fingerprint
        if (createMode && BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            showEnableFingerprintDialog();
        }

        // Navigate to the password fragment
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentFragment, new PasswordFragment());
        transaction.commit();
    }
}
