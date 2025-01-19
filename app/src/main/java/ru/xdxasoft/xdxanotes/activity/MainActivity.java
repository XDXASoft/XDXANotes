package ru.xdxasoft.xdxanotes.activity;

import static ru.xdxasoft.xdxanotes.utils.LocaleHelper.setLocale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.UUID;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.fragments.AccountFragment;
import ru.xdxasoft.xdxanotes.fragments.CalendarFragment;
import ru.xdxasoft.xdxanotes.fragments.NotesFragment;
import ru.xdxasoft.xdxanotes.fragments.SettingsFragment;
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

    private Handler handler;
    private Runnable runnable;

    private FirebaseAuth mAuth;


    BottomNavigationView bottomNavigationView;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        ThemeManager.applyTheme(this);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        String _android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        openFragment(new NotesFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.notes) {
                selectedFragment = new NotesFragment();
            } else if (item.getItemId() == R.id.navigation_search) {
                selectedFragment = new CalendarFragment();
            } else if (item.getItemId() == R.id.navigation_settings) {
                selectedFragment = new SettingsFragment();
            } else if (item.getItemId() == R.id.navigation_test) {
                selectedFragment = new AccountFragment();
            }


            if (selectedFragment != null) {
                openFragment(selectedFragment);
            }
            return true;
        });

        checkURL();

        //CustomDialog.showCustomDialog(this, "w1", "Это кастомный диалог!");

        tets();


    }

    public void tets(){
        Intent intent = getIntent();
        if (intent.getData() != null && intent.getData().getPath().equals("/test")) {
            String errCode = "E100";
            ToastManager.showToast(this, "Ошибка подключения!\nКод ошибки: " + errCode, R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);

        }

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }
    }

    public void checkURL(){
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

    public void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.contentFragment, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
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
