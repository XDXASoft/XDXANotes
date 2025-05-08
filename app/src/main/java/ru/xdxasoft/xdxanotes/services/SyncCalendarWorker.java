package ru.xdxasoft.xdxanotes.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.DataBase.RoomDB;

public class SyncCalendarWorker extends Worker {

    private static final String TAG = "SyncCalendarWorker";

    public SyncCalendarWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Синхронизация событий календаря");

            RoomDB database = RoomDB.getInstance(getApplicationContext());
            FirebaseManager firebaseManager = FirebaseManager.getInstance(getApplicationContext());

            if (firebaseManager.isUserLoggedIn()) {
                firebaseManager.syncCalendarEventsWithFirebase(success -> {
                    if (success) {
                        Log.d(TAG, "Синхронизация с Firebase успешна");
                    } else {
                        Log.e(TAG, "Ошибка синхронизации с Firebase");
                    }
                });
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при синхронизации событий", e);
            return Result.failure();
        }
    }
}