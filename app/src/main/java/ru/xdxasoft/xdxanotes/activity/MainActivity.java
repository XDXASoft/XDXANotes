package ru.xdxasoft.xdxanotes.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class MainActivity extends AppCompatActivity {



    private Spinner languageSpinner;
    private Button applyButton;
    private FirebaseAuth mAuth;



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
        checkUserSession();

        languageSpinner = findViewById(R.id.languageSpinner);
        applyButton = findViewById(R.id.applyButton);


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


    }




    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            Toast.makeText(MainActivity.this, "Сессия активна!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, AuthSelectionActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Сессия не активна. Пожалуйста, войдите в систему.", Toast.LENGTH_SHORT).show();
        }
    }

    public void testerrtoast(View v){
        String err_code = "E100";

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);
        ToastManager.showToast(this, "Ошибка подключения!\nКод ошибки: "  + err_code, R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);
        ToastManager.showToast(this, "Предупреждение!\nДелить на 0 нельзя", R.drawable.warning_black, Color.YELLOW, Color.BLACK, Color.BLACK);
        ToastManager.showToast(this, "Аккаунт успешно создан!", R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);

    }

    public void test2(View v){
        String email = "snovamgodam55@gmail.com";

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



}