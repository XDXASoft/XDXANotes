package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.messaging.FirebaseMessaging;

public class FcmCheckWorker extends Worker {

    private static final String TAG = "FCM_HEALTH";

    public FcmCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Проверка FCM соединения...");

        // Запрос нового токена для обновления соединения с FCM
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FCM соединение активно, токен: "
                                + task.getResult().substring(0, 5) + "...");
                    } else {
                        Log.e(TAG, "Ошибка FCM соединения: ", task.getException());
                    }
                });

        return Result.success();
    }
}
