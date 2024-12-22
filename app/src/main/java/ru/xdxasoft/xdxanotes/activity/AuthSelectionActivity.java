package ru.xdxasoft.xdxanotes.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.units.qual.A;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;

public class AuthSelectionActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private EditText editText;
    private EditText mail, pass;

    private Button btn;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();


        btn = findViewById(R.id.login_btn);



        mail = findViewById(R.id.logintext);
        pass = findViewById(R.id.passtext);

        findViewById(R.id.main).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mail.isFocused() && mail.isFocused()) {
                    hideKeyboard();
                    mail.clearFocus();
                    pass.clearFocus();
                }
            }
            return false;
        });

        btn.setOnClickListener(v -> {
            if(mail.getText().toString().isEmpty() || pass.getText().toString().isEmpty())
            {
                Toast.makeText(AuthSelectionActivity.this, "Введите логин и пароль!", Toast.LENGTH_LONG).show();

            }else{

                auth.signInWithEmailAndPassword(mail.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Intent intent = new Intent(AuthSelectionActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else{

                            Toast.makeText(AuthSelectionActivity.this, "Неверный логин или пароль!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mail.getWindowToken(), 0);
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
}