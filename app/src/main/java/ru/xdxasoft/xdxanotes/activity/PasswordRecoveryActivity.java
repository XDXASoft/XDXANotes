package ru.xdxasoft.xdxanotes.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class PasswordRecoveryActivity extends AppCompatActivity {

    Button recoverypass_btn;
    private FirebaseAuth mAuth;
    EditText mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_recovery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout toastContainer = findViewById(R.id.toastContainer);
        ToastManager.init(toastContainer);

        mAuth = FirebaseAuth.getInstance();

        recoverypass_btn = findViewById(R.id.recoverypass_btn);
        mail = findViewById(R.id.mailtext);

        recoverypass_btn.setOnClickListener(v ->{
            String email = mail.getText().toString();

            if (email == null || email.isEmpty()) {
                ToastManager.showToast(this, "Введите адрес электронной почты", R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ToastManager.showToast(this, "Ссылка для сброса пароля отправлена на почту", R.drawable.ic_galohca_black, Color.GREEN, Color.BLACK, Color.BLACK);

                        } else {
                            ToastManager.showToast(this, "Ошибка при отправке ссылки", R.drawable.ic_error_black, Color.RED, Color.BLACK, Color.BLACK);
                        }
                    });
        });
    }
}