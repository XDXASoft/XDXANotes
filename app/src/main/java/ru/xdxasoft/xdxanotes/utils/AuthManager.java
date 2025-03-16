package ru.xdxasoft.xdxanotes.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.Color;

import ru.xdxasoft.xdxanotes.R;

public class AuthManager {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public interface OnRegistrationListener {

        void onSuccess(String message);

        void onFailure(String error);
    }

    public void registerUser(String email, String password, String username, OnRegistrationListener listener) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> signInMethods = task.getResult().getSignInMethods();
                if (signInMethods != null && !signInMethods.isEmpty()) {
                    // Если email существует, удаляем старый аккаунт
                    deleteExistingAccountAndRegister(email, password, username, listener);
                } else {
                    // Если email новый, регистрируем
                    createNewAccount(email, password, username, listener);
                }
            } else {
                listener.onFailure("Ошибка проверки email");
            }
        });
    }

    private void deleteExistingAccountAndRegister(String email, String password, String username, OnRegistrationListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(signInTask -> {
                    if (signInTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    createNewAccount(email, password, username, listener);
                                } else {
                                    listener.onFailure("Ошибка удаления существующего аккаунта");
                                }
                            });
                        }
                    } else {
                        listener.onFailure("Этот email уже зарегистрирован");
                    }
                });
    }

    private void createNewAccount(String email, String password, String username, OnRegistrationListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Отправляем письмо для верификации
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            // Сохраняем информацию о пользователе
                                            User newUser = new User(username, email);
                                            mDatabase.child("Users").child(user.getUid()).setValue(newUser)
                                                    .addOnCompleteListener(databaseTask -> {
                                                        if (databaseTask.isSuccessful()) {
                                                            mAuth.signOut();
                                                            listener.onSuccess("Регистрация успешна! Проверьте почту для подтверждения.");
                                                        } else {
                                                            listener.onFailure("Ошибка сохранения данных пользователя");
                                                        }
                                                    });
                                        } else {
                                            listener.onFailure("Ошибка отправки письма подтверждения");
                                        }
                                    });
                        }
                    } else {
                        listener.onFailure("Ошибка регистрации: " + task.getException().getMessage());
                    }
                });
    }

    public void resendVerificationEmail(OnRegistrationListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSuccess("Письмо подтверждения отправлено повторно");
                        } else {
                            listener.onFailure("Ошибка отправки письма");
                        }
                    });
        }
    }

    public void sendVerificationEmail(Context context) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ToastManager.showToast(context,
                                    "Письмо подтверждения отправлено на " + user.getEmail(),
                                    R.drawable.ic_galohca_black,
                                    Color.GREEN,
                                    Color.BLACK,
                                    Color.BLACK);
                        } else {
                            ToastManager.showToast(context,
                                    "Ошибка отправки письма: " + task.getException().getMessage(),
                                    R.drawable.ic_error,
                                    Color.RED,
                                    Color.BLACK,
                                    Color.BLACK);
                        }
                    });
        }
    }

    public void checkEmailVerification(Context context, FirebaseUser user) {
        if (user != null) {
            if (!user.isEmailVerified()) {
                CustomDialogHelper.showSimpleDialog(
                        context,
                        "Подтверждение email",
                        "Ваш email не подтвержден. Хотите получить письмо с подтверждением повторно?",
                        "Да",
                        Color.parseColor("#727272"),
                        (dialog, which) -> {
                            sendVerificationEmail(context);
                            dialog.dismiss();
                        },
                        "Отмена",
                        Color.RED,
                        (dialog, which) -> {
                            mAuth.signOut();
                            dialog.dismiss();
                        }
                );
            }
        }
    }
}
