package ru.xdxasoft.xdxanotes.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void registerUser(String email, String password, String username, OnRegistrationListener listener) {
        // Сначала проверяем существование email
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult result = task.getResult();
                        if (result != null && result.getSignInMethods() != null
                                && !result.getSignInMethods().isEmpty()) {
                            // Email существует, удаляем старый аккаунт
                            deleteOldAccountAndCreate(email, password, username, listener);
                        } else {
                            // Email не существует, создаем новый аккаунт
                            createNewAccount(email, password, username, listener);
                        }
                    } else {
                        listener.onFailure("Ошибка при проверке email");
                    }
                });
    }

    private void deleteOldAccountAndCreate(String email, String password, String username,
            OnRegistrationListener listener) {
        // Создаем временные учетные данные
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // Пытаемся удалить существующий аккаунт через админский SDK
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Удаляем данные из базы данных
                        mDatabase.child("Users").child(user.getUid()).removeValue()
                                .addOnCompleteListener(removeTask -> {
                                    // Удаляем аккаунт
                                    user.delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // После успешного удаления создаем новый
                                                createNewAccount(email, password, username, listener);
                                            })
                                            .addOnFailureListener(e -> {
                                                mAuth.signOut();
                                                createNewAccount(email, password, username, listener);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    mAuth.signOut();
                                    createNewAccount(email, password, username, listener);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Если не удалось войти, все равно пробуем создать новый
                    createNewAccount(email, password, username, listener);
                });
    }

    private void createNewAccount(String email, String password, String username,
            OnRegistrationListener listener) {
        // Сначала пытаемся удалить, если вдруг остался
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser oldUser = authResult.getUser();
                    if (oldUser != null) {
                        oldUser.delete().addOnCompleteListener(deleteTask -> {
                            actuallyCreateAccount(email, password, username, listener);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    actuallyCreateAccount(email, password, username, listener);
                });
    }

    private void actuallyCreateAccount(String email, String password, String username,
            OnRegistrationListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnSuccessListener(aVoid -> {
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("username", username);
                                    userData.put("email", user.getEmail());
                                    userData.put("verified", false);
                                    userData.put("userId", user.getUid());
                                    userData.put("createdAt", System.currentTimeMillis());

                                    mDatabase.child("Users").child(user.getUid())
                                            .setValue(userData)
                                            .addOnSuccessListener(unused -> {
                                                mAuth.signOut();
                                                listener.onSuccess("Регистрация успешна! Проверьте почту для подтверждения");
                                            })
                                            .addOnFailureListener(e -> {
                                                user.delete();
                                                mAuth.signOut();
                                                listener.onFailure("Ошибка при сохранении данных");
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    user.delete();
                                    mAuth.signOut();
                                    listener.onFailure("Ошибка при отправке письма подтверждения");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onFailure("Ошибка при создании аккаунта: " + e.getMessage());
                });
    }

    public interface OnRegistrationListener {

        void onSuccess(String message);

        void onFailure(String error);
    }
}
