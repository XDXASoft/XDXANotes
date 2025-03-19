package ru.xdxasoft.xdxanotes.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Форсируем работу через WakeLock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FCM:MessageWakeLock");

        wakeLock.acquire(60 * 1000); // 3 минуты для обработки

        try {
            // Показываем уведомление с максимальным приоритетом
            NotificationManager notificationManager
                    = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "urgent_channel")
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(remoteMessage.getNotification() != null
                            ? remoteMessage.getNotification().getTitle() : "Новое сообщение")
                    .setContentText(remoteMessage.getNotification() != null
                            ? remoteMessage.getNotification().getBody() : "Проверьте приложение")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "urgent_channel",
                        "Срочные уведомления",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.enableVibration(true);
                channel.setBypassDnd(true);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(1, builder.build());
        } finally {
            wakeLock.release();
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Новый токен: " + token);
    }
}
