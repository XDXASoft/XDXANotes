package ru.xdxasoft.xdxanotes.utils.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;

public class NotesTakerActivity extends AppCompatActivity {

    private static final String TAG = "NotesTakerActivity";

    EditText editText_title, editText_notes;
    ImageView imageView_save;
    Notes notes;
    boolean isOldNote = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        try {
            editText_title = findViewById(R.id.editText_title);
            editText_notes = findViewById(R.id.editText_notes);
            imageView_save = findViewById(R.id.imageView_save);

            TextView toolbarTitle = findViewById(R.id.toolbar_title);

            // Проверяем, редактируем ли существующую заметку
            notes = new Notes();
            try {
                notes = (Notes) getIntent().getSerializableExtra("old_note");
                if (notes != null) {
                    editText_title.setText(notes.getTitle());
                    editText_notes.setText(notes.getNotes());
                    isOldNote = true;

                    // Меняем заголовок, если редактируем существующую заметку
                    toolbarTitle.setText(R.string.edit_note);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting old note", e);
            }

            imageView_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String title = editText_title.getText().toString();
                        String description = editText_notes.getText().toString();

                        if (description.isEmpty()) {
                            Toast.makeText(NotesTakerActivity.this, "Please add some notes!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault());
                        Date date = new Date();

                        if (!isOldNote) {
                            notes = new Notes();
                            // Генерируем случайный ID для новой заметки
                            notes.setID(new Random().nextInt(1000000) + 1);
                            // Устанавливаем время создания только для новых заметок
                            notes.setDate(formatter.format(date));
                            // Устанавливаем userId для новой заметки
                            FirebaseManager firebaseManager = FirebaseManager.getInstance(NotesTakerActivity.this);
                            if (firebaseManager.isUserLoggedIn()) {
                                notes.setUserId(firebaseManager.getUserId());
                            }
                        }

                        notes.setTitle(title);
                        notes.setNotes(description);

                        Log.d(TAG, "Saving note: ID=" + notes.getID() + ", Title=" + notes.getTitle());

                        Intent intent = new Intent();
                        intent.putExtra("note", notes);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving note", e);
                        Toast.makeText(NotesTakerActivity.this, "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Ошибка инициализации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
