package ru.xdxasoft.xdxanotes.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class RegActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private static FirebaseDatabase firebaseDatabase;
    private Button regbtn;
    private TextView mailreg, passreg, usernamereg;
    private static FirebaseAuth mauth;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        regbtn = findViewById(R.id.reg_btn);
        mailreg = findViewById(R.id.logintext);
        passreg = findViewById(R.id.passtext);
        usernamereg = findViewById(R.id.usernametext);
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

        passreg.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

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
            String username = usernamereg.getText().toString();
            String email = mailreg.getText().toString();
            String password = passreg.getText().toString();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                ToastManager.showToast(RegActivity.this,"Введите имя пользователя, логин и пароль!",R.drawable.ic_error,Color.RED,Color.BLACK,Color.BLACK);
            } else {
                checkUsernameAndRegister(username, email, password);
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

    public static boolean isUsernameAvailable(String username) {
        final boolean[] isAvailable = {true};

        if (firebaseDatabase == null) {
            return false;
        }

        DatabaseReference usersRef = firebaseDatabase.getReference("Users");
        Query query = usersRef.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isAvailable[0] = false;
                    System.out.println("Пользователь с username " + username + " уже зарегистрирован");
                } else {
                    isAvailable[0] = true;
                    System.out.println("Пользователь с username " + username + " доступен для регистрации");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Ошибка при проверке username: " + databaseError.getMessage());
                isAvailable[0] = false;
            }
        });

        return isAvailable[0];
    }


    private void createUserWithEmailAndPassword(String email, String password, String username) {
        mauth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mauth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, username);
                            saveUserDataToDatabase(user.getUid(), email, password, username);
                            Intent intent = new Intent(RegActivity.this, MainActivity.class);
                            intent.putExtra("ACCOUNT_LOGIN", false);
                            intent.putExtra("ACCOUNT_MAIL", mailreg.getText().toString());
                            intent.putExtra("ACCOUNT_USERNAME", usernamereg.getText().toString());
                            startActivity(intent);
                        } else {
                            ToastManager.showToast(RegActivity.this, "Не удалось зарегистрировать пользователя", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                        }
                    } else {
                        ToastManager.showToast(RegActivity.this, "Не удалось зарегистрировать пользователя", R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
                    }
                });
    }

    private void updateUserProfile(FirebaseUser user, String username) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        user.updateProfile(profileUpdates);
    }

    private void saveUserDataToDatabase(String userId, String email, String password, String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.child("email").setValue(email);
        userRef.child("password").setValue(password);
        userRef.child("username").setValue(username);

        DatabaseReference idRef = FirebaseDatabase.getInstance().getReference("LastUserId");
        idRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long lastId = dataSnapshot.getValue(Long.class);
                if (lastId == null) {
                    lastId = 0L; // Если ID не найден, начинаем с 0
                }
                Long newId = lastId + 1; // Увеличиваем ID на 1
                userRef.child("id").setValue(newId);
                idRef.setValue(newId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastManager.showToast(RegActivity.this, "Ошибка при получении ID: " + databaseError.getMessage(), R.drawable.ic_error, Color.RED, Color.BLACK, Color.BLACK);
            }
        });
    }

    private void checkUsernameAndRegister(String username, String email, String password) {
        DatabaseReference usersRef = firebaseDatabase.getReference("Users");
        Query query = usersRef.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ToastManager.showToast(
                            RegActivity.this,
                            "Пользователь с таким именем уже зарегистрирован!",
                            R.drawable.ic_error,
                            Color.RED,
                            Color.BLACK,
                            Color.BLACK
                    );
                } else {
                    createUserWithEmailAndPassword(email, password, username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastManager.showToast(
                        RegActivity.this,
                        "Ошибка проверки имени пользователя: " + databaseError.getMessage(),
                        R.drawable.ic_error,
                        Color.RED,
                        Color.BLACK,
                        Color.BLACK
                );
            }
        });
    }





}