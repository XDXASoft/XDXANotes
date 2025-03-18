package ru.xdxasoft.xdxanotes.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.fragments.PasswordFragment;
import ru.xdxasoft.xdxanotes.fragments.CalendarFragment;
import ru.xdxasoft.xdxanotes.fragments.NotesFragment;
import ru.xdxasoft.xdxanotes.fragments.SettingsFragment;
import ru.xdxasoft.xdxanotes.utils.AuthDialogHelper;
import ru.xdxasoft.xdxanotes.utils.CustomDialogHelper;
import ru.xdxasoft.xdxanotes.utils.DialogLauncher;
import ru.xdxasoft.xdxanotes.utils.LinkApprovalChecker;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.ThemeManager;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.User;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;

    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }



        EdgeToEdge.enable(this);
        ThemeManager.applyTheme(this);
        LocaleHelper.applyLanguage(this);

        checkSystemLanguage();

        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signInAnonymously();
        } else {
            // Инициализируем FirebaseManager
            FirebaseManager.getInstance(this);
        }


        CustomDialogHelper.showSimpleDialog(
                this,
                "Заголовок",
                "Сообщение",
                "ОК",
                Color.parseColor("#727272"),
                (dialog, which) -> {
                    // Действие при нажатии ОК
                },
                "Отмена",
                Color.RED,
                (dialog, which) -> dialog.dismiss()
        );

        String _android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                    new int[]{android.R.attr.state_checked}, // состояние активного элемента
                    new int[]{} // состояние неактивного элемента
                },
                new int[]{
                    Color.parseColor("#484848"), // цвет для активного элемента
                    Color.parseColor("#484848") // цвет для неактивного элемента
                }
        );

        bottomNavigationView.setItemActiveIndicatorColor(colorStateList); // Устанавливаем tint для иконок

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_notes) {
                selectedFragment = new NotesFragment();
            } else if (item.getItemId() == R.id.navigation_passwordmanager) {
                selectedFragment = new PasswordFragment();
            } else if (item.getItemId() == R.id.navigation_settings) {
                selectedFragment = new SettingsFragment();
            }else if (item.getItemId() == R.id.navigation_schedule) {
                selectedFragment = new CalendarFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFragment, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Устанавливаем NotesFragment как фрагмент по умолчанию
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFragment, new NotesFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.navigation_notes);
        }

        checkURL();

        //CustomDialog.showCustomDialog(this, "w1", "Это кастомный диалог!");
        tets();
    }

    private void checkSystemLanguage() {
        try {
            // Если выбрано использование языка системы
            if (LocaleHelper.isUsingSystemLanguage(this)) {
                String currentLanguage = LocaleHelper.getLanguage(this);
                String systemLanguage = LocaleHelper.getSystemLanguage();

                // Если язык системы изменился, обновляем язык приложения
                if (!currentLanguage.equals(systemLanguage)) {
                    Log.d(TAG, "Язык системы изменился с " + currentLanguage + " на " + systemLanguage);
                    LocaleHelper.setLocale(this, systemLanguage);
                    recreate(); // Пересоздаем активность для применения изменений
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке языка системы: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Проверяем язык системы при возобновлении активности
        checkSystemLanguage();
    }

    public void tets() {
        Intent intent = getIntent();
        if (intent.getData() != null && intent.getData().getPath().equals("/test")) {
            String errCode = "E100";
            ToastManager.showToast(this, "Ошибка подключения!\nКод ошибки: " + errCode, R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);
        }

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }
    }

    public void checkURL() {
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
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Успешная анонимная авторизация
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Инициализируем FirebaseManager
                            FirebaseManager.getInstance(this);
                        }
                    } else {
                        // Ошибка авторизации
                        Toast.makeText(MainActivity.this, "Ошибка авторизации: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
}
