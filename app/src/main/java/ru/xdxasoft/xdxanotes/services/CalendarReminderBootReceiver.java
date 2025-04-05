package ru.xdxasoft.xdxanotes.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Получатель широковещательного сообщения о загрузке системы. Запускает сервис
 * напоминаний календаря при загрузке устройства.
 */
public class CalendarReminderBootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null
                && (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)
                || intent.getAction().equals(Intent.ACTION_REBOOT))) {

            Log.d(TAG, "Устройство загружено, запускаем сервис календарных напоминаний");

            // Запускаем сервис напоминаний
            Intent serviceIntent = new Intent(context, CalendarReminderService.class);

            // Для Android 8.0 (API 26) и выше нельзя запускать фоновые сервисы напрямую
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
