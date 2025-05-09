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
    private static final long SYNC_INTERVAL = 1 * 60 * 1000;

    private ScheduledExecutorService scheduler;
    private RoomDB database;
    private FirebaseManager firebaseManager;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Сервис календарных напоминаний запущен");

        Executors.newSingleThreadExecutor().execute(() -> {
            database = RoomDB.getInstance(this);
            firebaseManager = FirebaseManager.getInstance(this);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            createNotificationChannel();

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::syncEvents, 0, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(this::checkUpcomingEvents, 0, 60, TimeUnit.SECONDS);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Напоминания о событиях")
                .setContentText("Сервис работает в фоновом режиме")
                .setSmallIcon(R.drawable.ic_calendar)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);

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
                    getString(R.string.Event_Reminders),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.Channel_for_calendar_event_notifications));
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void syncEvents() {
        try {
            Log.d(TAG, "Синхронизация событий календаря");
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

            Calendar now = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentDate = dateFormat.format(now.getTime());

            List<CalendarEvent> todayEvents = database.calendarDao().getEventsByDate(currentDate);

            for (CalendarEvent event : todayEvents) {
                if (event.isCompleted()) {
                    continue;
                }

                try {
                    Date eventTime = timeFormat.parse(event.getTime());
                    Date currentTime = timeFormat.parse(timeFormat.format(now.getTime()));

                    if (eventTime == null || currentTime == null) {
                        continue;
                    }

                    long diffInMillis = eventTime.getTime() - currentTime.getTime();
                    long diffInMinutes = diffInMillis / (60 * 1000);

                    if ((diffInMinutes >= 0 && diffInMinutes <= 5) || event.getNotificationType() == 2) {
                        showEventNotification(event);
                    }

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
        int notificationId;
        if (event.getEventId() != null && !event.getEventId().isEmpty()) {
            notificationId = event.getEventId().hashCode();
        } else {
            notificationId = event.getID() + 2000;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("openCalendar", true);
        intent.putExtra("eventDate", event.getDate());

        if (event.getEventId() != null && !event.getEventId().isEmpty()) {
            intent.putExtra("eventId", event.getEventId());
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder;

        if (event.getNotificationType() == 2) {
            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setContentTitle(getString(R.string.All_day) + ": " + event.getTitle())
                    .setContentText(event.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setTimeoutAfter(-1);
        } else {
            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setContentTitle(event.getTitle())
                    .setContentText(event.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        notificationManager.notify(notificationId, builder.build());
    }
}