package ru.xdxasoft.xdxanotes.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import android.content.Context;
import java.util.concurrent.TimeUnit;

public class FcmGuarantee {

    // Принудительное отключение режима Doze
    public static void disableBatteryOptimizations(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = activity.getPackageName();
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                activity.startActivity(intent);
            }
        }
    }

    // Запуск регулярной проверки FCM
    public static void setupFcmHealthCheck(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest fcmCheckWork
                = new PeriodicWorkRequest.Builder(FcmCheckWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "fcm_health_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                fcmCheckWork);
    }
}
