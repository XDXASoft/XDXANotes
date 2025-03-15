package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PasswordDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "passwords.db";
    private static final int DATABASE_VERSION = 2;

    public PasswordDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS passwords ("
                + "id TEXT PRIMARY KEY, "
                + "title TEXT, "
                + "username TEXT, "
                + "password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Миграция с числовых ID на строковые
            db.execSQL("ALTER TABLE passwords RENAME TO passwords_old");
            db.execSQL("CREATE TABLE passwords ("
                    + "id TEXT PRIMARY KEY, "
                    + "title TEXT, "
                    + "username TEXT, "
                    + "password TEXT)");
            db.execSQL("INSERT INTO passwords (id, title, username, password) "
                    + "SELECT CAST(id AS TEXT), title, username, password FROM passwords_old");
            db.execSQL("DROP TABLE passwords_old");
        }
    }
}
