package ru.xdxasoft.xdxanotes.utils.firebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.xdxasoft.xdxanotes.models.Password;
import ru.xdxasoft.xdxanotes.utils.PasswordDatabaseHelper;
import ru.xdxasoft.xdxanotes.utils.notes.DataBase.RoomDB;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private final Context context;
    private String userId;
    private RoomDB notesDatabase;
    private SQLiteDatabase passwordsDatabase;
    private PasswordDatabaseHelper dbHelper;

    private FirebaseManager(Context context) {
        this.context = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        notesDatabase = RoomDB.getInstance(context);
        dbHelper = new PasswordDatabaseHelper(context);
        passwordsDatabase = dbHelper.getWritableDatabase();
    }

    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context);
        }
        return instance;
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public String getUserId() {
        return userId;
    }

    // ===== NOTES METHODS =====
    public void syncNotesWithFirebase(final SyncCallback callback) {
        if (!isUserLoggedIn()) {
            if (callback != null) {
                callback.onSyncComplete(false);
            }
            return;
        }

        mDatabase.child("Users").child(userId).child("notes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // Получаем заметки из Firebase
                    Map<String, Notes> firebaseNotes = new HashMap<>();
                    for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Notes note = noteSnapshot.getValue(Notes.class);
                            if (note != null && note.getID() > 0) {
                                firebaseNotes.put(String.valueOf(note.getID()), note);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing note from Firebase", e);
                        }
                    }

                    // Получаем локальные заметки
                    List<Notes> localNotes = notesDatabase.mainDao().getAll();
                    Map<String, Notes> localNotesMap = new HashMap<>();

                    for (Notes localNote : localNotes) {
                        localNotesMap.put(String.valueOf(localNote.getID()), localNote);
                    }

                    // Добавляем заметки из Firebase, которых нет локально
                    for (Map.Entry<String, Notes> entry : firebaseNotes.entrySet()) {
                        if (!localNotesMap.containsKey(entry.getKey())) {
                            try {
                                notesDatabase.mainDao().insert(entry.getValue());
                                Log.d(TAG, "Inserted note from Firebase: " + entry.getValue().getTitle());
                            } catch (Exception e) {
                                Log.e(TAG, "Error inserting note from Firebase", e);
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onSyncComplete(true);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing notes", e);
                    if (callback != null) {
                        callback.onSyncComplete(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase sync cancelled", databaseError.toException());
                if (callback != null) {
                    callback.onSyncComplete(false);
                }
            }
        });
    }

    public void saveNoteToFirebase(Notes note, final SaveCallback callback) {
        if (!isUserLoggedIn() || note == null) {
            if (callback != null) {
                callback.onSaveComplete(false);
            }
            return;
        }

        try {
            // Создаем копию заметки для Firebase
            Map<String, Object> noteValues = new HashMap<>();
            noteValues.put("ID", note.getID());
            noteValues.put("title", note.getTitle());
            noteValues.put("notes", note.getNotes());
            noteValues.put("date", note.getDate());
            noteValues.put("pinned", note.isPinned());

            mDatabase.child("Users").child(userId).child("notes").child(String.valueOf(note.getID()))
                    .setValue(noteValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Note saved to Firebase: " + note.getTitle());
                        if (callback != null) {
                            callback.onSaveComplete(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving note to Firebase", e);
                        if (callback != null) {
                            callback.onSaveComplete(false);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing note for Firebase", e);
            if (callback != null) {
                callback.onSaveComplete(false);
            }
        }
    }

    public void deleteNoteFromFirebase(Notes note, final DeleteCallback callback) {
        if (!isUserLoggedIn() || note == null) {
            if (callback != null) {
                callback.onDeleteComplete(false);
            }
            return;
        }

        mDatabase.child("Users").child(userId).child("notes").child(String.valueOf(note.getID())).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Note deleted from Firebase: " + note.getTitle());
                    if (callback != null) {
                        callback.onDeleteComplete(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting note from Firebase", e);
                    if (callback != null) {
                        callback.onDeleteComplete(false);
                    }
                });
    }

    // ===== PASSWORDS METHODS =====
    public void syncPasswordsWithFirebase(final SyncCallback callback) {
        if (!isUserLoggedIn()) {
            if (callback != null) {
                callback.onSyncComplete(false);
            }
            return;
        }

        mDatabase.child("Users").child(userId).child("passwords").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // Получаем пароли из Firebase
                    Map<String, Password> firebasePasswords = new HashMap<>();
                    for (DataSnapshot passwordSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Password password = passwordSnapshot.getValue(Password.class);
                            if (password != null && password.getId() != null && !password.getId().isEmpty()) {
                                firebasePasswords.put(password.getId(), password);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing password from Firebase", e);
                        }
                    }

                    // Получаем локальные пароли
                    Cursor cursor = passwordsDatabase.rawQuery("SELECT * FROM passwords", null);
                    Map<String, Password> localPasswordsMap = new HashMap<>();

                    while (cursor.moveToNext()) {
                        String id = cursor.getString(0);
                        Password password = new Password(
                                id,
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3)
                        );
                        localPasswordsMap.put(id, password);
                    }
                    cursor.close();

                    // Добавляем пароли из Firebase, которых нет локально
                    boolean dataChanged = false;
                    for (Map.Entry<String, Password> entry : firebasePasswords.entrySet()) {
                        if (!localPasswordsMap.containsKey(entry.getKey())) {
                            try {
                                Password password = entry.getValue();
                                ContentValues cv = new ContentValues();
                                cv.put("id", password.getId());
                                cv.put("title", password.getTitle());
                                cv.put("username", password.getUsername());
                                cv.put("password", password.getPassword());
                                passwordsDatabase.insert("passwords", null, cv);
                                dataChanged = true;
                                Log.d(TAG, "Inserted password from Firebase: " + password.getTitle());
                            } catch (Exception e) {
                                Log.e(TAG, "Error inserting password from Firebase", e);
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onSyncComplete(true);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing passwords", e);
                    if (callback != null) {
                        callback.onSyncComplete(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase sync cancelled", databaseError.toException());
                if (callback != null) {
                    callback.onSyncComplete(false);
                }
            }
        });
    }

    public void savePasswordToFirebase(Password password, final SaveCallback callback) {
        if (!isUserLoggedIn() || password == null) {
            if (callback != null) {
                callback.onSaveComplete(false);
            }
            return;
        }

        try {
            // Создаем копию пароля для Firebase
            Map<String, Object> passwordValues = new HashMap<>();
            passwordValues.put("id", password.getId());
            passwordValues.put("title", password.getTitle());
            passwordValues.put("username", password.getUsername());
            passwordValues.put("password", password.getPassword());

            mDatabase.child("Users").child(userId).child("passwords").child(password.getId())
                    .setValue(passwordValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Password saved to Firebase: " + password.getTitle());
                        if (callback != null) {
                            callback.onSaveComplete(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving password to Firebase", e);
                        if (callback != null) {
                            callback.onSaveComplete(false);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing password for Firebase", e);
            if (callback != null) {
                callback.onSaveComplete(false);
            }
        }
    }

    public void deletePasswordFromFirebase(String id, final DeleteCallback callback) {
        if (!isUserLoggedIn() || id == null || id.isEmpty()) {
            if (callback != null) {
                callback.onDeleteComplete(false);
            }
            return;
        }

        mDatabase.child("Users").child(userId).child("passwords").child(id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Password deleted from Firebase: " + id);
                    if (callback != null) {
                        callback.onDeleteComplete(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting password from Firebase", e);
                    if (callback != null) {
                        callback.onDeleteComplete(false);
                    }
                });
    }

    // ===== CALLBACK INTERFACES =====
    public interface SyncCallback {

        void onSyncComplete(boolean success);
    }

    public interface SaveCallback {

        void onSaveComplete(boolean success);
    }

    public interface DeleteCallback {

        void onDeleteComplete(boolean success);
    }
}
