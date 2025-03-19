package ru.xdxasoft.xdxanotes.activity;

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
import ru.xdxasoft.xdxanotes.utils.User;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Короткая задержка для показа сплэш-экрана
        new Handler().postDelayed(this::checkUserAndNavigate, 2000);
    }

    private void checkUserAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Если пользователь не авторизован, перенаправляем на экран входа
            navigateToLogin(false);
        } else {
            // Проверяем принятие политики конфиденциальности
            checkPrivacyAcceptanceFromDatabase();
        }
    }

    // Метод для проверки принятия политики из базы данных
    private void checkPrivacyAcceptanceFromDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Ищем пользователя в базе данных по email текущего пользователя
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
                                // Если политика принята, переходим на главный экран
                                navigateToMain();
                            } else {
                                // Если политика не принята, возвращаем на экран входа с флагом
                                FirebaseAuth.getInstance().signOut();
                                navigateToLogin(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Обработка ошибки - перенаправляем на экран входа при проблеме доступа к данным
                            Log.e(TAG, "Ошибка при проверке принятия политики: " + databaseError.getMessage());
                            FirebaseAuth.getInstance().signOut();
                            navigateToLogin(false);
                        }
                    });
        }
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
