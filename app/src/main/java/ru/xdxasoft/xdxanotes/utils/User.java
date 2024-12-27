package ru.xdxasoft.xdxanotes.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User {

    private String email;   // Email пользователя
    private String username;  // Имя пользователя
    private String password;  // Пароль пользователя
    private Long id;  // ID пользователя, храним как Long

    // Конструктор по умолчанию для Firebase
    public User() {
    }

    // Конструктор с параметрами, если необходимо создавать объект вручную
    public User(String email, String username, String password, Long id) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.id = id;
    }

    // Геттер для email
    public String getEmail() {
        return email;
    }

    // Сеттер для email
    public void setEmail(String email) {
        this.email = email;
    }

    // Геттер для username
    public String getUsername() {
        return username;
    }

    // Сеттер для username
    public void setUsername(String username) {
        this.username = username;
    }

    // Геттер для password
    public String getPassword() {
        return password;
    }

    // Сеттер для password
    public void setPassword(String password) {
        this.password = password;
    }

    // Геттер для id
    public Long getId() {
        return id;
    }

    // Сеттер для id
    public void setId(Long id) {
        this.id = id;
    }

    // Метод для загрузки данных пользователя по UID
    public static void loadUser(String uid, final OnUserLoadedCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    callback.onUserLoaded(user);
                } else {
                    callback.onError("User data not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Error loading user data: " + databaseError.getMessage());
            }
        });
    }

    // Интерфейс обратного вызова для работы с загрузкой пользователя
    public interface OnUserLoadedCallback {
        void onUserLoaded(User user);
        void onError(String errorMessage);
    }
}
