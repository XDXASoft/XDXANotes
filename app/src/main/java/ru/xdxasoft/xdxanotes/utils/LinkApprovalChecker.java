package ru.xdxasoft.xdxanotes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.verify.domain.DomainVerificationManager;
import android.content.pm.verify.domain.DomainVerificationUserState;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import ru.xdxasoft.xdxanotes.R;

public class LinkApprovalChecker {

    public static boolean isLinkSwitchEnabled(Context context) {
        // Обновляем контекст с нужной локалью
        Context localizedContext = LocaleHelper.applyLanguage(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                @SuppressLint("WrongConstant")
                DomainVerificationManager domainVerificationManager =
                        (DomainVerificationManager) localizedContext.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE);

                if (domainVerificationManager != null) {
                    DomainVerificationUserState userState =
                            domainVerificationManager.getDomainVerificationUserState(localizedContext.getPackageName());

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
        // Обновляем контекст с нужной локалью
        Context localizedContext = LocaleHelper.applyLanguage(context);

        ToastManager.showToast(localizedContext,
                localizedContext.getResources().getString(R.string.To_use_the_application_enable_the_Open_supported_links_option),
                R.drawable.warning_black,
                Color.YELLOW,
                Color.BLACK,
                Color.BLACK);

        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + localizedContext.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Добавляем флаг для перехода
            localizedContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastManager.showToast(localizedContext,
                    localizedContext.getResources().getString(R.string.Failed_to_open_settings_Please_check_your_settings_manually),
                    R.drawable.ic_error,
                    Color.RED,
                    Color.BLACK,
                    Color.BLACK);
        }
    }
}