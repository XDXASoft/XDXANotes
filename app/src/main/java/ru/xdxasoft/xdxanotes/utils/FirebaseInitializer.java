package ru.xdxasoft.xdxanotes.utils;

import android.app.Application;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import ru.xdxasoft.xdxanotes.R;

public class FirebaseInitializer {
    private static final String TAG = "FCM_INIT";

    public static void initialize(Application application) {
        Application localizedApplication = (Application) LocaleHelper.applyLanguage(application);

        FirebaseApp.initializeApp(localizedApplication);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM TOKEN: " + token);
                        ToastManager.showToast(localizedApplication,
                                "TOKEN: " + token,
                                R.drawable.ic_galohca_black,
                                ContextCompat.getColor(localizedApplication, R.color.success_green),
                                ContextCompat.getColor(localizedApplication, R.color.black),
                                ContextCompat.getColor(localizedApplication, R.color.black),
                                true);
                    } else {
                        Log.e(TAG, "Ошибка получения токена", task.getException());
                        ToastManager.showToast(localizedApplication,
                                application.getString(R.string.Error_getting_token),
                                R.drawable.ic_error,
                                ContextCompat.getColor(localizedApplication, R.color.error_red),
                                ContextCompat.getColor(localizedApplication, R.color.black),
                                ContextCompat.getColor(localizedApplication, R.color.black),
                                true);
                    }
                });
    }
}