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
import android.util.Log;
import java.util.concurrent.TimeUnit;

import ru.xdxasoft.xdxanotes.R;

public class AuthManager {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    // Время последней отправки верификационного письма
    private static long lastVerificationEmailSentTime = 0;
    // Минимальный интервал между отправками писем (в минутах)
    private static final long MIN_INTERVAL_MINUTES = 2;

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public interface OnRegistrationListener {

        void onSuccess(String message);

        void onFailure(String error);
    }

    public void registerUser(String email, String password, OnRegistrationListener listener) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> signInMethods = task.getResult().getSignInMethods();
                if (signInMethods != null && !signInMethods.isEmpty()) {
                    // Если email существует, удаляем старый аккаунт
                    deleteExistingAccountAndRegister(email, password, listener);
                } else {
                    // Если email новый, регистрируем
                    createNewAccount(email, password, listener);
                }
            } else {
                listener.onFailure("Ошибка проверки email");
            }
        });
    }

    private void deleteExistingAccountAndRegister(String email, String password, OnRegistrationListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(signInTask -> {
                    if (signInTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    createNewAccount(email, password, listener);
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

    private void createNewAccount(String email, String password, OnRegistrationListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Проверяем, не слишком ли часто отправляются письма
                            if (canSendVerificationEmail()) {
                                // Отправляем письмо для верификации
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                // Сохраняем время отправки
                                                lastVerificationEmailSentTime = System.currentTimeMillis();

                                                // Сохраняем информацию о пользователе
                                                User newUser = new User(email);
                                                // При регистрации через email автоматически устанавливаем флаг принятия политики
                                                newUser.setPrivacyAccepted(true);
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
                                                String errorMessage = "Ошибка отправки письма подтверждения";
                                                if (verificationTask.getException() != null) {
                                                    errorMessage = handleVerificationError(verificationTask.getException().getMessage());
                                                    Log.e("EMAIL_VERIFICATION", "Ошибка: " + verificationTask.getException().getMessage());
                                                }
                                                listener.onFailure(errorMessage);
                                            }
                                        });
                            } else {
                                // Если письма отправляются слишком часто
                                long timeToWait = MIN_INTERVAL_MINUTES - getMinutesSinceLastEmail();
                                listener.onFailure("Слишком много запросов. Пожалуйста, подождите " + timeToWait + " мин.");
                            }
                        }
                    } else {
                        listener.onFailure("Ошибка регистрации: " + task.getException().getMessage());
                    }
                });
    }

    public void resendVerificationEmail(OnRegistrationListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Проверяем, можно ли отправить письмо
            if (canSendVerificationEmail()) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Обновляем время последней отправки
                                lastVerificationEmailSentTime = System.currentTimeMillis();
                                listener.onSuccess("Письмо подтверждения отправлено повторно");
                            } else {
                                String errorMessage = "Ошибка отправки письма";
                                if (task.getException() != null) {
                                    errorMessage = handleVerificationError(task.getException().getMessage());
                                    Log.e("EMAIL_VERIFICATION", "Ошибка отправки письма: " + task.getException().getMessage());
                                }
                                listener.onFailure(errorMessage);
                            }
                        });
            } else {
                // Если отправка слишком частая
                long timeToWait = MIN_INTERVAL_MINUTES - getMinutesSinceLastEmail();
                listener.onFailure("Слишком много запросов. Пожалуйста, подождите " + timeToWait + " мин.");
            }
        }
    }

    public void sendVerificationEmail(Context context) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Проверяем, можно ли отправить письмо
            if (canSendVerificationEmail()) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Обновляем время последней отправки
                                lastVerificationEmailSentTime = System.currentTimeMillis();
                                ToastManager.showToast(context,
                                        "Письмо подтверждения отправлено на " + user.getEmail(),
                                        R.drawable.ic_galohca_black,
                                        Color.GREEN,
                                        Color.BLACK,
                                        Color.BLACK);
                            } else {
                                String errorMessage = "Ошибка отправки письма";
                                if (task.getException() != null) {
                                    errorMessage = handleVerificationError(task.getException().getMessage());
                                }
                                ToastManager.showToast(context,
                                        errorMessage,
                                        R.drawable.ic_error,
                                        Color.RED,
                                        Color.BLACK,
                                        Color.BLACK);
                            }
                        });
            } else {
                // Если отправка слишком частая
                long timeToWait = MIN_INTERVAL_MINUTES - getMinutesSinceLastEmail();
                ToastManager.showToast(context,
                        "Слишком много запросов. Пожалуйста, подождите " + timeToWait + " мин.",
                        R.drawable.ic_error,
                        Color.RED,
                        Color.BLACK,
                        Color.BLACK);
            }
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

    // Проверяет, можно ли отправить верификационное письмо
    private boolean canSendVerificationEmail() {
        // Если письмо никогда не отправлялось или прошло достаточно времени
        return lastVerificationEmailSentTime == 0
                || getMinutesSinceLastEmail() >= MIN_INTERVAL_MINUTES;
    }

    // Вычисляет, сколько минут прошло с момента последней отправки
    private long getMinutesSinceLastEmail() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastVerificationEmailSentTime;
        return TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
    }

    // Обрабатывает сообщения об ошибках Firebase и возвращает понятное сообщение
    private String handleVerificationError(String errorMessage) {
        if (errorMessage.contains("blocked all requests")
                || errorMessage.contains("unusual activity")) {
            return "Слишком много запросов с этого устройства. Попробуйте позже.";
        } else if (errorMessage.contains("quota exceeded")) {
            return "Превышен лимит отправки писем. Попробуйте позже.";
        } else {
            return "Ошибка отправки письма: " + errorMessage;
        }
    }
}
