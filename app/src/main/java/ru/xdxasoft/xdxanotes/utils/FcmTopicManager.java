package ru.xdxasoft.xdxanotes.utils;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FcmTopicManager {

    private static final String TAG = "FcmTopicManager";

    public static void subscribeToTopic(String topicName) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Подписка на топик " + topicName + " успешна");
                    } else {
                        Log.e(TAG, "Ошибка подписки: ", task.getException());
                    }
                });
    }

    public static void unsubscribeFromTopic(String topicName) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Отписка от топика " + topicName + " успешна");
                    } else {
                        Log.e(TAG, "Ошибка отписки: ", task.getException());
                    }
                });
    }
}

