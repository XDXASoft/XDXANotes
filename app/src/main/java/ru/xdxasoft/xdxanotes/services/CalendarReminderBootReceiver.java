package ru.xdxasoft.xdxanotes.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class CalendarReminderBootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null
                && (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)
                || intent.getAction().equals(Intent.ACTION_REBOOT))) {

            Log.d(TAG, "Устройство загружено, планируем задачу синхронизации");

            OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncCalendarWorker.class)
                    .build();
            WorkManager.getInstance(context).enqueue(syncRequest);
        }
    }
}