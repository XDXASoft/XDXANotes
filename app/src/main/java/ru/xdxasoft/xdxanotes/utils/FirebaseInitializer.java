package ru.xdxasoft.xdxanotes.utils;

import android.app.Application;
import android.app.PendingIntent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.LoginActivity;
import ru.xdxasoft.xdxanotes.activity.MainActivity;

public class FirebaseInitializer {
    private static final String TAG = "FCM_INIT";


    public static void initialize(Application application) {
        FirebaseApp.initializeApp(application);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM TOKEN: " + token);
                        ToastManager.showToast(application,
                                "TOKEN: " + token,
                                R.drawable.ic_galohca_black,
                                Color.GREEN,
                                Color.BLACK,
                                Color.BLACK,
                                true);
                    } else {
                        Log.e(TAG, "Ошибка получения токена", task.getException());

                    }
                });
    }
}
