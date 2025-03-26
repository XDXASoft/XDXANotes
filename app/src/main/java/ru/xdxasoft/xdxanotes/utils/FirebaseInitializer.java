package ru.xdxasoft.xdxanotes.utils;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import ru.xdxasoft.xdxanotes.R;

public class FirebaseInitializer {
    private static final String TAG = "FCM_INIT";

    public static void initialize(Application application) {
        // Обновляем контекст с нужной локалью
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
                                Color.GREEN,
                                Color.BLACK,
                                Color.BLACK,
                                true);
                    } else {
                        Log.e(TAG, "Ошибка получения токена", task.getException());
                        ToastManager.showToast(localizedApplication,
                                "Ошибка получения токена",
                                R.drawable.ic_error,
                                Color.RED,
                                Color.BLACK,
                                Color.BLACK,
                                true);
                    }
                });
    }
}