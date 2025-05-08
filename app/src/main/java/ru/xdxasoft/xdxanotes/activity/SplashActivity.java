package ru.xdxasoft.xdxanotes.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.User;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private FirebaseAuth mAuth;
    private FirebaseManager firebaseManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_splash);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseManager = FirebaseManager.getInstance(this);

        new Handler().postDelayed(this::checkUserAndNavigate, 2000);
    }

    private void checkUserAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin(false);
        } else {
            checkPrivacyAcceptanceFromDatabase();
        }
    }

    private void checkPrivacyAcceptanceFromDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.orderByChild("email").equalTo(currentUser.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean privacyAccepted = false;

                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    User user = snapshot.getValue(User.class);
                                    if (user != null && user.isPrivacyAccepted()) {
                                        privacyAccepted = true;
                                        break;
                                    }
                                }
                            }

                            if (privacyAccepted) {
                                syncDataAndNavigate();
                            } else {
                                FirebaseAuth.getInstance().signOut();
                                navigateToLogin(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Ошибка при проверке принятия политики: " + databaseError.getMessage());
                            FirebaseAuth.getInstance().signOut();
                            navigateToLogin(false);
                        }
                    });
        }
    }

    private void syncDataAndNavigate() {
        firebaseManager.syncCalendarEventsWithFirebase(success -> {
            if (success) {
                Log.d(TAG, "Синхронизация календарных событий успешно завершена");
            } else {
                Log.e(TAG, "Ошибка при синхронизации календарных событий");
            }

            firebaseManager.syncNotesWithFirebase(notesSuccess -> {
                if (notesSuccess) {
                    Log.d(TAG, "Синхронизация заметок успешно завершена");
                } else {
                    Log.e(TAG, "Ошибка при синхронизации заметок");
                }

                firebaseManager.syncPasswordsWithFirebase(passwordsSuccess -> {
                    if (passwordsSuccess) {
                        Log.d(TAG, "Синхронизация паролей успешно завершена");
                    } else {
                        Log.e(TAG, "Ошибка при синхронизации паролей");
                    }

                    navigateToMain();
                });
            });
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin(boolean privacyNotAccepted) {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        if (privacyNotAccepted) {
            intent.putExtra("PRIVACY_NOT_ACCEPTED", true);
        }
        startActivity(intent);
        finish();
    }
}
