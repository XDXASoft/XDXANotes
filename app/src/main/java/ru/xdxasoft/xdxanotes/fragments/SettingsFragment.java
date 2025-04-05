package ru.xdxasoft.xdxanotes.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.LoginActivity;
import ru.xdxasoft.xdxanotes.activity.MainActivity;
import ru.xdxasoft.xdxanotes.activity.RegActivity;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;

/**
 * Фрагмент настроек приложения. Позволяет управлять аккаунтом пользователя и
 * менять языковые настройки.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    // UI компоненты
    private TextView tvUserEmail, tvAuthMethod, tvCurrentLanguage, tvLanguageMode;
    private Button btnLogout, btnToggleLanguage, btnSystemLanguage;

    // Firebase компоненты
    private FirebaseAuth firebaseAuth;
    private FirebaseManager firebaseManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseManager = FirebaseManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        setupListeners();
        updateUserInfo();
        updateLanguageInfo();

        return view;
    }

    private void initViews(View view) {
        // Инициализация компонентов пользовательского интерфейса
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvAuthMethod = view.findViewById(R.id.tvAuthMethod);
        tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage);
        tvLanguageMode = view.findViewById(R.id.tvLanguageMode);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnToggleLanguage = view.findViewById(R.id.btnToggleLanguage);
        btnSystemLanguage = view.findViewById(R.id.btnSystemLanguage);
    }

    private void setupListeners() {
        // Настройка обработчиков событий

        // Выход из аккаунта
        btnLogout.setOnClickListener(v -> {
            logoutUser();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            MainActivity mainActivity = new MainActivity();
            mainActivity.finish();
        });

        // Переключение языка
        btnToggleLanguage.setOnClickListener(v -> {
            if (getActivity() != null) {
                LocaleHelper.toggleLanguage(getActivity());
            }
        });

        // Использование системного языка
        btnSystemLanguage.setOnClickListener(v -> {
            if (getActivity() != null) {
                LocaleHelper.useSystemLanguage(getActivity());
            }
        });
    }

    private void updateUserInfo() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Установка email пользователя
            String email = currentUser.getEmail();
            tvUserEmail.setText(getString(R.string.email) + ": " + (email != null ? email : getString(R.string.not_available)));

            // Определение метода авторизации
            String authMethod = getAuthMethod(currentUser);
            tvAuthMethod.setText(getString(R.string.auth_method) + ": " + authMethod);

            btnLogout.setEnabled(true);
        } else {
            // Пользователь не авторизован
            tvUserEmail.setText(getString(R.string.not_logged_in));
            tvAuthMethod.setText(getString(R.string.auth_method) + ": " + getString(R.string.not_available));
            btnLogout.setEnabled(false);
        }
    }

    private String getAuthMethod(FirebaseUser user) {
        List<? extends UserInfo> providerData = user.getProviderData();

        for (UserInfo userInfo : providerData) {
            String providerId = userInfo.getProviderId();

            if (providerId.equals("password")) {
                return getString(R.string.email_password);
            } else if (providerId.equals("google.com")) {
                return "Google";
            } else if (providerId.equals("github.com")) {
                return "GitHub";
            } else if (providerId.equals("facebook.com")) {
                return "Facebook";
            } else if (providerId.equals("phone")) {
                return getString(R.string.phone_number);
            }
        }

        return getString(R.string.unknown);
    }

    private void updateLanguageInfo() {
        try {
            String currentLanguage = LocaleHelper.getLanguage(requireContext());
            String languageName = currentLanguage.equals("ru") ? "Русский" : "English";
            tvCurrentLanguage.setText(getString(R.string.current_language) + ": " + languageName);

            boolean useSystemLanguage = LocaleHelper.isUsingSystemLanguage(requireContext());
            String modeText = useSystemLanguage
                    ? getString(R.string.using_system_language)
                    : getString(R.string.using_custom_language);
            tvLanguageMode.setText(modeText);
        } catch (Exception e) {
            Log.e(TAG, "Error updating language info", e);
        }
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        showSnackbar(getString(R.string.logged_out_successfully));
    }

    private void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserInfo();
        updateLanguageInfo();
    }
}
