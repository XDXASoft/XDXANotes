package ru.xdxasoft.xdxanotes.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: Получено сообщение");

        // Проверяем, есть ли данные в сообщении
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Извлекаем данные (например, title, body, и type) из сообщения
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type"); // Дополнительный параметр для определения типа сообщения

            // Если данные присутствуют, проверяем тип сообщения
            if (type != null) {
                if (type.equals("in_app")) {
                    // Если это in-app сообщение, показываем его в приложении
                    showInAppMessage(title, body);
                } else {
                    // Если это обычное уведомление, показываем уведомление
                    sendNotification(title, body);
                }
            } else {
                Log.d(TAG, "Нет данных для уведомления");
            }
        } else {
            Log.d(TAG, "Нет данных в сообщении");
        }

        // Обрабатываем стандартное уведомление, если оно есть
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification("FCM Message", remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token); // Отправляем новый токен на сервер
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Реализовать отправку токена на сервер
        Log.d(TAG, "Token sent to server: " + token);
    }

    private void sendNotification(String title, String messageBody) {
        // Создаем Intent для открытия MainActivity при клике на уведомление
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Строим уведомление
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_galohca_black)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Создаем канал уведомлений для Android Oreo и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Отправляем уведомление
        notificationManager.notify(0 /* ID уведомления */, notificationBuilder.build());
    }

    private void showInAppMessage(String title, String body) {
        // Показываем in-app сообщение с помощью Toast
        Toast.makeText(getApplicationContext(), title + ": " + body, Toast.LENGTH_LONG).show();

        Log.d(TAG, title + ": " + body);
        // Вы можете также использовать другие способы отображения сообщений внутри приложения:
        // Например, показывать диалог или фрагмент с сообщением:
        // new AlertDialog.Builder(this)
        //         .setTitle(title)
        //         .setMessage(body)
        //         .setPositiveButton("OK", null)
        //         .show();
    }
}
