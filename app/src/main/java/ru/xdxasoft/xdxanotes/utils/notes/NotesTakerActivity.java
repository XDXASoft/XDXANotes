package ru.xdxasoft.xdxanotes.utils.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;

public class NotesTakerActivity extends AppCompatActivity {

    private static final String TAG = "NotesTakerActivity";

    EditText editText_title, editText_notes;
    ImageView imageView_save;
    Notes notes;
    boolean isOldNote = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_notes_taker);

        try {
            editText_title = findViewById(R.id.editText_title);
            editText_notes = findViewById(R.id.editText_notes);
            imageView_save = findViewById(R.id.imageView_save);

            TextView toolbarTitle = findViewById(R.id.toolbar_title);

            notes = new Notes();
            try {
                notes = (Notes) getIntent().getSerializableExtra("old_note");
                if (notes != null) {
                    editText_title.setText(notes.getTitle());
                    editText_notes.setText(notes.getNotes());
                    isOldNote = true;

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
                            Toast.makeText(NotesTakerActivity.this, getString(R.string.Please_add_some_notes), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault());
                        Date date = new Date();

                        if (!isOldNote) {
                            notes = new Notes();
                            notes.setID(new Random().nextInt(1000000) + 1);
                            notes.setDate(formatter.format(date));
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
                        Toast.makeText(NotesTakerActivity.this, getString(R.string.Error_saving_note), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(NotesTakerActivity.this, getString(R.string.Initialization_error), Toast.LENGTH_SHORT).show();
        }
    }
}