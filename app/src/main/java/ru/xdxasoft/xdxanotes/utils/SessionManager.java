package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public void savePasswordHash(String passwordHash) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password_hash", passwordHash);
        editor.apply();
    }

    public String getPasswordHash() {
        return sharedPreferences.getString("password_hash", null);
    }
}
