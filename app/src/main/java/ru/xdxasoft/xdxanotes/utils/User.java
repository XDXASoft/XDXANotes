package ru.xdxasoft.xdxanotes.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User {

    private String email;
    private String username;
    private String password;
    private Long id;
    private String service;
    private boolean privacyAccepted = false;

    public User() {
        // Пустой конструктор нужен для Firebase
    }

    public User(String email) {
        this.email = email;
    }

    public User(String email, String service) {
        this.email = email;
        this.service = service;
    }

    public User(String email, String service, boolean privacyAccepted) {
        this.email = email;
        this.service = service;
        this.privacyAccepted = privacyAccepted;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public boolean isPrivacyAccepted() {
        return privacyAccepted;
    }

    public void setPrivacyAccepted(boolean privacyAccepted) {
        this.privacyAccepted = privacyAccepted;
    }

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

    public interface OnUserLoadedCallback {

        void onUserLoaded(User user);

        void onError(String errorMessage);
    }
}
