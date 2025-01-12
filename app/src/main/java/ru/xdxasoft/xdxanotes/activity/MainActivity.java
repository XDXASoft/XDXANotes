package ru.xdxasoft.xdxanotes.activity;

import static ru.xdxasoft.xdxanotes.utils.LocaleHelper.setLocale;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.services.PasswordValidationService;
import ru.xdxasoft.xdxanotes.utils.CustomDialog;
import ru.xdxasoft.xdxanotes.utils.LinkApprovalChecker;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.PreferencesHelper;
import ru.xdxasoft.xdxanotes.utils.ThemeManager;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;
import ru.xdxasoft.xdxanotes.utils.ValidationUtils;

public class MainActivity extends AppCompatActivity {

    private EditText testEditText;
    private Spinner languageSpinner;
    private Handler handler;
    private Runnable runnable;

    private FirebaseRemoteConfig remoteConfig;
    private Button applyButton;
    private FirebaseAuth mAuth;
    private TextView test123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        ThemeManager.applyTheme(this);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        boolean isSwitchEnabled = LinkApprovalChecker.isLinkSwitchEnabled(this);

        if (isSwitchEnabled) {

            checkUserSession();
        } else {
            LinkApprovalChecker.promptToEnableLinkHandling(this);

            Intent intent = new Intent(this, CheckDomanUrlActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        //CustomDialog.showCustomDialog(this, "w1", "Это кастомный диалог!");



        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        test123 = findViewById(R.id.test123);

        Intent intent = getIntent();
        if (intent.getData() != null && intent.getData().getPath().equals("/test")) {
            String errCode = "E100";
            ToastManager.showToast(this, "Ошибка подключения!\nКод ошибки: " + errCode, R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);

        }


        configureRemoteConfig();

        // Загрузка данных пользователя, если он авторизован
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }

    }



    public void switchTheme(int theme) {
        ThemeManager.saveTheme(this, theme);
        recreate(); // Перезагружаем Activity для применения темы
    }

    public void ysdad(View v){
        LocaleHelper.toggleLanguage(this);
        switchTheme(ThemeManager.THEME_DARK);

        recreate();

    }

    private void configureRemoteConfig() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(600)
                .build());
        startFetchingRemoteConfig();
    }

    private void startFetchingRemoteConfig() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchRemoteConfig();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }

    private void fetchRemoteConfig() {
        if (ValidationUtils.isNetworkAvailable(this)) {
            remoteConfig.fetchAndActivate()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String value = remoteConfig.getString("test");
                            Log.d("RemoteConfig", "Value for test key: " + value);
                            test123.setText(value);
                            test123.setTextSize(30);
                        } else {
                            Log.e("RemoteConfig", "Fetch failed", task.getException());
                        }
                    });
        } else {
            test123.setText("Internet is not active");
            test123.setTextSize(30);
        }
    }

    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "Сессия активна!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, AuthSelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Сессия не активна. Пожалуйста, войдите в систему.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            User.loadUser(uid, new User.OnUserLoadedCallback() {
                @Override
                public void onUserLoaded(User user) {
                    if (user != null) {
                        Log.d("MainActivity", "Email пользователя: " + user.getEmail());
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("MainActivity", "Ошибка загрузки пользователя: " + errorMessage);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public void testerrtoast(View v) {
        String errCode = "E100";
        ToastManager.showToast(this, "Ошибка подключения!\nКод ошибки: " + errCode, R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);
    }

    public void test2(View v) {
        String email = testEditText.getText().toString();

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Введите адрес электронной почты", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Ссылка для сброса пароля отправлена на почту", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка при отправке ссылки", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void test(View v) {
        mAuth.signOut();
        Intent intent = new Intent(this, AuthSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
