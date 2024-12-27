package ru.xdxasoft.xdxanotes.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;
import ru.xdxasoft.xdxanotes.utils.ValidationUtils;

public class MainActivity extends AppCompatActivity {


    private EditText testedittext;
    private Spinner languageSpinner;
    private Handler handler;
    private Runnable runnable;

    private FirebaseRemoteConfig remoteConfig;
    private Button applyButton;
    private FirebaseAuth mAuth;
    private TextView test123;



    @Override
    protected void attachBaseContext(Context newBase) {
        String deviceLanguage = Locale.getDefault().getLanguage();
        String language = LocaleHelper.getLanguage(newBase);

        if (language == null || language.isEmpty()) {
            language = deviceLanguage;
        }

        Context context = LocaleHelper.setLocale(newBase, language);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

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

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);





        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        User.loadUser(uid, new User.OnUserLoadedCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    boolean accountlogin = getIntent().getBooleanExtra("ACCOUNT_LOGIN", false);
                    String ACCOUNT_MAIL = getIntent().getStringExtra("ACCOUNT_MAIL");
                    if (!accountlogin && ACCOUNT_MAIL != null) {
                        ToastManager.showToast(MainActivity.this, "Успешный вход в аккаунт: " + ACCOUNT_MAIL, R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });

        test123 = findViewById(R.id.test123);
        remoteConfig = FirebaseRemoteConfig.getInstance();
        checkUserSession();

        languageSpinner = findViewById(R.id.languageSpinner);
        applyButton = findViewById(R.id.applyButton);
        testedittext = findViewById(R.id.testedittext);


        final String[] languages = {"en", "ru"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        languageSpinner.setAdapter(adapter);

        String currentLanguage = LocaleHelper.getLanguage(this);
        int spinnerPosition = adapter.getPosition(currentLanguage);
        languageSpinner.setSelection(spinnerPosition);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedLanguage = languageSpinner.getSelectedItem().toString();
                LocaleHelper.setLocale(MainActivity.this, selectedLanguage);
                recreate();
            }
        });
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
                    .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()) {
                                Boolean updated = task.getResult();
                                Log.d("RemoteConfig", "Config params updated: " + updated);

                                String value = remoteConfig.getString("test");
                                Log.d("RemoteConfig", "Value for example_key: " + value);

                                test123.setText(value);
                            } else {
                                Log.e("RemoteConfig", "Fetch failed");
                            }
                        }
                    });
        }else{
            test123.setText("Internet is not activity");
            test123.setTextSize(30);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }




    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            Toast.makeText(MainActivity.this, "Сессия активна!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, AuthSelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            boolean isSessionActive = false;
            intent.putExtra("SESSION_ACTIVE", isSessionActive);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Сессия не активна. Пожалуйста, войдите в систему.", Toast.LENGTH_SHORT).show();
        }
    }

    public void testerrtoast(View v){
        String err_code = "E100";

        ToastManager.showToast(this, "Ошибка подключения!\nКод ошибки: "  + err_code, R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);
        ToastManager.showToast(this, "Предупреждение!\nДелить на 0 нельзя", R.drawable.warning_black, Color.YELLOW, Color.BLACK, Color.BLACK);
        ToastManager.showToast(this, "Аккаунт успешно создан!", R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);

    }

    public void test2(View v){
        String email = testedittext.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите адрес электронной почты", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ResetPassword", "Email sent.");
                        Toast.makeText(MainActivity.this, "Ссылка для сброса пароля отправлена на почту", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ResetPassword", "Error sending email", task.getException());
                        Toast.makeText(MainActivity.this, "Ошибка при отправке ссылки", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void test(View v){
        mAuth.signOut();
        Intent intent = new Intent(this, AuthSelectionActivity.class);
        startActivity(intent);
    }



}