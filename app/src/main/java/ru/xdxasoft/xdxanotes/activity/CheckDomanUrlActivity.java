package ru.xdxasoft.xdxanotes.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LinkApprovalChecker;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;

public class CheckDomanUrlActivity extends AppCompatActivity {


    private Handler handler;
    private Runnable runnable;

    private final Handler handler1 = new Handler(Looper.getMainLooper());
    private boolean isChecking = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_check_doman_url);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startLinkSwitchCheck();

    }

    private void startLinkSwitchCheck() {
        isChecking = true;

        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isChecking) return;

                boolean isSwitchEnabled = LinkApprovalChecker.isLinkSwitchEnabled(CheckDomanUrlActivity.this);

                if (isSwitchEnabled) {
                    isChecking = false;
                    Intent intent = new Intent(CheckDomanUrlActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    handler1.postDelayed(this, 5000);
                }
            }
        }, 5000);
    }

    public void settingsactivity(View v){
        Intent intent = new Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}