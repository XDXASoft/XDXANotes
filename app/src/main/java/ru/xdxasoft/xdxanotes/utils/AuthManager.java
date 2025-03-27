package ru.xdxasoft.xdxanotes.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import ru.xdxasoft.xdxanotes.R;

public class AuthManager {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static long lastVerificationEmailSentTime = 0;
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
                    deleteExistingAccountAndRegister(email, password, listener);
                } else {
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
                            if (canSendVerificationEmail()) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                lastVerificationEmailSentTime = System.currentTimeMillis();

                                                User newUser = new User(email);
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
            if (canSendVerificationEmail()) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
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
                long timeToWait = MIN_INTERVAL_MINUTES - getMinutesSinceLastEmail();
                listener.onFailure("Слишком много запросов. Пожалуйста, подождите " + timeToWait + " мин.");
            }
        }
    }

    public void sendVerificationEmail(Context context) {
        Context localizedContext = LocaleHelper.applyLanguage(context);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (canSendVerificationEmail()) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                lastVerificationEmailSentTime = System.currentTimeMillis();
                                ToastManager.showToast(localizedContext,
                                        localizedContext.getString(R.string.Confirmation_email_sent_to) + user.getEmail(),
                                        R.drawable.ic_galohca_black,
                                        ContextCompat.getColor(localizedContext, R.color.success_green),
                                        ContextCompat.getColor(localizedContext, R.color.black),
                                        ContextCompat.getColor(localizedContext, R.color.black));
                            } else {
                                String errorMessage = localizedContext.getString(R.string.Error_sending_email);
                                if (task.getException() != null) {
                                    errorMessage = handleVerificationError(task.getException().getMessage());
                                }
                                ToastManager.showToast(localizedContext,
                                        errorMessage,
                                        R.drawable.ic_error,
                                        ContextCompat.getColor(localizedContext, R.color.error_red),
                                        ContextCompat.getColor(localizedContext, R.color.black),
                                        ContextCompat.getColor(localizedContext, R.color.black));
                            }
                        });
            } else {
                long timeToWait = MIN_INTERVAL_MINUTES - getMinutesSinceLastEmail();
                ToastManager.showToast(localizedContext,
                        localizedContext.getString(R.string.Too_many_requests_Please_wait) + timeToWait + " мин.",
                        R.drawable.ic_error,
                        ContextCompat.getColor(localizedContext, R.color.error_red),
                        ContextCompat.getColor(localizedContext, R.color.black),
                        ContextCompat.getColor(localizedContext, R.color.black));
            }
        }
    }

    public void checkEmailVerification(Context context, FirebaseUser user) {
        Context localizedContext = LocaleHelper.applyLanguage(context);

        if (user != null) {
            if (!user.isEmailVerified()) {
                CustomDialogHelper.showSimpleDialog(
                        localizedContext,
                        localizedContext.getString(R.string.Confirmation_email),
                        localizedContext.getString(R.string.Your_email_is_not_confirmed_Do_you_want_to_receive_the_confirmation_email_again),
                        localizedContext.getString(R.string.Yes),
                        ContextCompat.getColor(localizedContext, R.color.dialog_neutral_gray),
                        (dialog, which) -> {
                            sendVerificationEmail(localizedContext);
                            dialog.dismiss();
                        },
                        localizedContext.getString(R.string.Cancel),
                        ContextCompat.getColor(localizedContext, R.color.error_red),
                        (dialog, which) -> {
                            mAuth.signOut();
                            dialog.dismiss();
                        }
                );
            }
        }
    }

    private boolean canSendVerificationEmail() {
        return lastVerificationEmailSentTime == 0
                || getMinutesSinceLastEmail() >= MIN_INTERVAL_MINUTES;
    }

    private long getMinutesSinceLastEmail() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastVerificationEmailSentTime;
        return TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
    }

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