package ru.xdxasoft.xdxanotes.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class RegActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Button regbtn;
    private TextView mailreg, passreg;
    private FirebaseAuth mauth;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg);

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        regbtn = findViewById(R.id.reg_btn);
        mailreg = findViewById(R.id.logintext);
        passreg = findViewById(R.id.passtext);
        mauth = FirebaseAuth.getInstance();

        passreg.setOnEditorActionListener((v, actionId, event) -> {
            Log.d("LoginActivity", "Editor action triggered");
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                passreg.clearFocus();
                regbtn.performClick();
                return true;
            }
            return false;
        });


        ScrollView scrollView = findViewById(R.id.scrollView);

        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focusedView = getCurrentFocus();
                if (focusedView instanceof EditText) {
                    focusedView.clearFocus();
                    hideKeyboard();
                }
            }
            return false;
        });


        findViewById(R.id.main).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mailreg.isFocused() || passreg.isFocused()) {
                    hideKeyboard();
                    mailreg.clearFocus();
                    passreg.clearFocus();
                }
            }
            return false;
        });



        regbtn.setOnClickListener(v -> {
            if (mailreg.getText().toString().isEmpty() || passreg.getText().toString().isEmpty()) {
                ToastManager.showToast(RegActivity.this, "Введите логин и пароль!", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);

            } else {
                mauth.createUserWithEmailAndPassword(mailreg.getText().toString(), passreg.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = mauth.getCurrentUser().getUid();
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                    userRef.child("email").setValue(mailreg.getText().toString());
                                    userRef.child("password").setValue(passreg.getText().toString());

                                    DatabaseReference idRef = FirebaseDatabase.getInstance().getReference("LastUserId");
                                    idRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Long lastId = dataSnapshot.getValue(Long.class);
                                            if (lastId == null) {
                                                lastId = 0L; // Если ID не найден, начинаем с 0
                                            }
                                            Long newId = lastId + 1; // Увеличиваем ID на 1

                                            // Сохранение нового ID для пользователя
                                            userRef.child("id").setValue(newId);

                                            // Обновление последнего использованного ID в базе данных
                                            idRef.setValue(newId);

                                            // Получение почты конкретного пользователя с UID "8BxEY8Aps1Vp6thI5RSewYQ7NJU2"
                                            String targetUserId = "8BxEY8Aps1Vp6thI5RSewYQ7NJU2";
                                            DatabaseReference targetUserRef = FirebaseDatabase.getInstance().getReference("Users").child(targetUserId);
                                            targetUserRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String email = dataSnapshot.getValue(String.class);
                                                    if (email != null) {
                                                       ToastManager.showToast(RegActivity.this, "Почта пользователя: " + email, R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);

                                                    } else {
                                                        ToastManager.showToast(RegActivity.this, "Почта не найдена", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    ToastManager.showToast(RegActivity.this, "Ошибка при получении почты: " + databaseError.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);

                                                }
                                            });

                                            Intent intent = new Intent(RegActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            ToastManager.showToast(RegActivity.this, "Ошибка при получении ID: " + databaseError.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);

                                        }
                                    });
                                } else {
                                    ToastManager.showToast(RegActivity.this, "Не удалось зарегистрировать пользователя", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);

                                }
                            }
                        });
            }
        });


    }

    public void LoginActivity(View v){
        Intent intent = new Intent(RegActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mailreg.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(passreg.getWindowToken(), 0);
        }
    }
}