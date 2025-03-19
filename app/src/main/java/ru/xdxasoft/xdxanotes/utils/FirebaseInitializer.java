package ru.xdxasoft.xdxanotes.utils;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseInitializer {

    private static final String TAG = "FCM_INIT";

    public static void initialize(Application application) {
        // Принудительная инициализация
        FirebaseApp.initializeApp(application);

        // Запрос токена и вывод в лог
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM TOKEN: " + token);
                        // Показываем токен
                        Toast.makeText(application, "TOKEN: " + token, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Ошибка получения токена", task.getException());
                        Toast.makeText(application, "Ошибка FCM: "
                                + (task.getException() != null ? task.getException().getMessage() : "неизвестная"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
