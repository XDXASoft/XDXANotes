package ru.xdxasoft.xdxanotes.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.MainActivity;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.DataBase.RoomDB;
import ru.xdxasoft.xdxanotes.utils.notes.Models.CalendarEvent;

public class CalendarReminderService extends Service {

    private static final String TAG = "CalendarReminder";
    private static final String CHANNEL_ID = "calendar_reminders";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;
    private static final long SYNC_INTERVAL = 1 * 60 * 1000; // 1 минут

    private ScheduledExecutorService scheduler;
    private RoomDB database;
    private FirebaseManager firebaseManager;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Сервис календарных напоминаний запущен");

        // Инициализация баз данных и сервисов
        database = RoomDB.getInstance(this);
        firebaseManager = FirebaseManager.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Создаем канал уведомлений
        createNotificationChannel();

        // Запускаем фоновую службу

        // Настраиваем планировщик задач
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Запускаем задачу синхронизации событий
        scheduler.scheduleAtFixedRate(this::syncEvents, 0, SYNC_INTERVAL, TimeUnit.MILLISECONDS);

        // Запускаем задачу проверки напоминаний
        scheduler.scheduleAtFixedRate(this::checkUpcomingEvents, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Будем перезапускать сервис, если он будет убит системой
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о событиях",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Канал для уведомлений о календарных событиях");
            notificationManager.createNotificationChannel(channel);
        }
    }



    private void syncEvents() {
        try {
            Log.d(TAG, "Синхронизация событий календаря");

            // Если пользователь авторизован, синхронизируем с Firebase
            if (firebaseManager.isUserLoggedIn()) {
                firebaseManager.syncCalendarEventsWithFirebase(success -> {
                    if (success) {
                        Log.d(TAG, "Синхронизация с Firebase успешна");
                    } else {
                        Log.e(TAG, "Ошибка синхронизации с Firebase");
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при синхронизации событий", e);
        }
    }

    private void checkUpcomingEvents() {
        try {
            Log.d(TAG, "Проверка предстоящих событий");

            // Получаем текущее время
            Calendar now = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentDate = dateFormat.format(now.getTime());

            // Получаем все события на сегодня
            List<CalendarEvent> todayEvents = database.calendarDao().getEventsByDate(currentDate);

            for (CalendarEvent event : todayEvents) {
                if (event.isCompleted()) {
                    continue; // Пропускаем завершенные события
                }

                try {
                    Date eventTime = timeFormat.parse(event.getTime());
                    Date currentTime = timeFormat.parse(timeFormat.format(now.getTime()));

                    if (eventTime == null || currentTime == null) {
                        continue;
                    }

                    // Разница между текущим временем и временем события в минутах
                    long diffInMillis = eventTime.getTime() - currentTime.getTime();
                    long diffInMinutes = diffInMillis / (60 * 1000);

                    // Если событие начинается в течение следующих 5 минут или тип уведомления - весь день
                    if ((diffInMinutes >= 0 && diffInMinutes <= 5) || event.getNotificationType() == 2) {
                        showEventNotification(event);
                    }

                    // Если у события есть отдельное время для уведомления
                    if (event.getNotificationType() == 1 && !event.getNotificationTime().isEmpty()) {
                        Date notificationTime = timeFormat.parse(event.getNotificationTime());

                        if (notificationTime != null) {
                            Date currentTimeWithoutSeconds = timeFormat.parse(timeFormat.format(now.getTime()));

                            if (currentTimeWithoutSeconds != null
                                    && Math.abs(notificationTime.getTime() - currentTimeWithoutSeconds.getTime()) < 60000) {
                                showEventNotification(event);
                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Ошибка при разборе времени события", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке предстоящих событий", e);
        }
    }

    private void showEventNotification(CalendarEvent event) {
        // Создаем уникальный ID для уведомления на основе eventId или ID события
        int notificationId;
        if (event.getEventId() != null && !event.getEventId().isEmpty()) {
            // Используем хеш-код eventId в качестве ID уведомления
            notificationId = event.getEventId().hashCode();
        } else {
            // Для обратной совместимости с событиями, у которых нет eventId
            notificationId = event.getID() + 2000;
        }

        // Создаем Intent для перехода к MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("openCalendar", true);
        intent.putExtra("eventDate", event.getDate());
        // Добавляем eventId для точного перехода к событию
        if (event.getEventId() != null && !event.getEventId().isEmpty()) {
            intent.putExtra("eventId", event.getEventId());
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder;

        if (event.getNotificationType() == 2) { // Если это целодневное уведомление
            // Для целодневных уведомлений создаем постоянное уведомление
            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setContentTitle("Весь день: " + event.getTitle())
                    .setContentText(event.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true) // Уведомление не может быть смахнуто пользователем
                    .setTimeoutAfter(-1); // Не истекает автоматически
        } else {
            // Для обычных уведомлений
            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setContentTitle(event.getTitle())
                    .setContentText(event.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true); // Удаляется при нажатии
        }

        notificationManager.notify(notificationId, builder.build());
    }
}
