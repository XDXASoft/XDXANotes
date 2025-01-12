package ru.xdxasoft.xdxanotes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.verify.domain.DomainVerificationManager;
import android.content.pm.verify.domain.DomainVerificationUserState;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class LinkApprovalChecker {

    public static boolean isLinkSwitchEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                @SuppressLint("WrongConstant")
                DomainVerificationManager domainVerificationManager =
                        (DomainVerificationManager) context.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE);

                if (domainVerificationManager != null) {
                    DomainVerificationUserState userState =
                            domainVerificationManager.getDomainVerificationUserState(context.getPackageName());

                    if (userState != null) {
                        return userState.isLinkHandlingAllowed();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void promptToEnableLinkHandling(Context context) {
        Toast.makeText(context, "Для работы приложения включите опцию 'Открывать поддерживаемые ссылки'.", Toast.LENGTH_LONG).show();

        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Добавляем флаг для перехода
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Не удалось открыть настройки. Проверьте параметры вручную.", Toast.LENGTH_LONG).show();
        }
    }
}
