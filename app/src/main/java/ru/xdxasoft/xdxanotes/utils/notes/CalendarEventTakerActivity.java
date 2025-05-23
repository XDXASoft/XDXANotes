package ru.xdxasoft.xdxanotes.utils.notes;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.Models.CalendarEvent;
import ru.xdxasoft.xdxanotes.utils.IdGenerator;

public class CalendarEventTakerActivity extends AppCompatActivity {

    private static final String TAG = "CalendarEventTaker";

    private EditText editTextTitle, editTextDescription;
    private TextView textViewDate, textViewTime;
    private Button buttonSave;
    private ImageView imageBack;
    private RadioGroup radioGroupNotificationType;
    private RadioButton radioButtonNoNotification, radioButtonOneTime;

    private String selectedDate = "";
    private String selectedTime = "";
    private int selectedNotificationType = 0;
    private boolean isEditMode = false;
    private CalendarEvent existingEvent;
    private FirebaseManager firebaseManager;
    private Calendar selectedCalendar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_calendar_event_taker);

        initViews();
        setupListeners();

        firebaseManager = FirebaseManager.getInstance(this);
        selectedCalendar = Calendar.getInstance();

        radioButtonNoNotification.setChecked(true);

        if (getIntent().hasExtra("event")) {
            isEditMode = true;
            existingEvent = (CalendarEvent) getIntent().getSerializableExtra("event");
            if (existingEvent != null) {
                editTextTitle.setText(existingEvent.getTitle());
                editTextDescription.setText(existingEvent.getDescription());
                selectedDate = existingEvent.getDate();
                selectedTime = existingEvent.getTime();
                textViewDate.setText(formatDisplayDate(selectedDate));
                textViewTime.setText(selectedTime);

                selectedNotificationType = existingEvent.getNotificationType();

                if (selectedNotificationType == 0) {
                    radioButtonNoNotification.setChecked(true);
                } else if (selectedNotificationType == 1) {
                    radioButtonOneTime.setChecked(true);
                }

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar eventCalendar = Calendar.getInstance();
                    Date eventDate = dateFormat.parse(selectedDate);
                    if (eventDate != null) {
                        eventCalendar.setTime(eventDate);

                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);

                        eventCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        eventCalendar.set(Calendar.MINUTE, 0);
                        eventCalendar.set(Calendar.SECOND, 0);
                        eventCalendar.set(Calendar.MILLISECOND, 0);

                        if (eventCalendar.before(today)) {
                            textViewDate.setEnabled(false);
                            textViewTime.setEnabled(false);

                            textViewDate.setAlpha(0.5f);
                            textViewTime.setAlpha(0.5f);

                            Toast.makeText(CalendarEventTakerActivity.this, getString(R.string.event_past_warning), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (getIntent().hasExtra("date")) {
            selectedDate = getIntent().getStringExtra("date");
            textViewDate.setText(formatDisplayDate(selectedDate));

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar eventCalendar = Calendar.getInstance();
                Date eventDate = dateFormat.parse(selectedDate);
                if (eventDate != null) {
                    eventCalendar.setTime(eventDate);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    eventCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    eventCalendar.set(Calendar.MINUTE, 0);
                    eventCalendar.set(Calendar.SECOND, 0);
                    eventCalendar.set(Calendar.MILLISECOND, 0);

                    if (eventCalendar.before(today)) {
                        Toast.makeText(CalendarEventTakerActivity.this, getString(R.string.event_create_past_warning), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(calendar.getTime());
            textViewDate.setText(formatDisplayDate(selectedDate));

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            minute = (minute / 30) * 30;
            calendar.set(Calendar.MINUTE, minute);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            selectedTime = timeFormat.format(calendar.getTime());
            textViewTime.setText(selectedTime);
        }
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.editTextEventTitle);
        editTextDescription = findViewById(R.id.editTextEventDescription);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        buttonSave = findViewById(R.id.buttonSaveEvent);
        imageBack = findViewById(R.id.imageBackEvent);
        radioGroupNotificationType = findViewById(R.id.radioGroupNotificationType);
        radioButtonNoNotification = findViewById(R.id.radioButtonNoNotification);
        radioButtonOneTime = findViewById(R.id.radioButtonOneTime);

        selectedCalendar = Calendar.getInstance();
    }

    private void setupListeners() {
        imageBack.setOnClickListener(v -> finish());

        textViewDate.setOnClickListener(v -> showDatePickerDialog());
        textViewTime.setOnClickListener(v -> showTimePickerDialog());

        radioGroupNotificationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonNoNotification) {
                selectedNotificationType = 0;
            } else if (checkedId == R.id.radioButtonOneTime) {
                selectedNotificationType = 1;
            }
        });

        buttonSave.setOnClickListener(v -> saveEvent());

        if (isEditMode && existingEvent != null) {
            editTextTitle.setText(existingEvent.getTitle());
            editTextDescription.setText(existingEvent.getDescription());
            selectedDate = existingEvent.getDate();
            selectedTime = existingEvent.getTime();
            textViewDate.setText(formatDisplayDate(selectedDate));
            textViewTime.setText(selectedTime);

            selectedNotificationType = existingEvent.getNotificationType();
            if (selectedNotificationType == 0) {
                radioButtonNoNotification.setChecked(true);
            } else if (selectedNotificationType == 1) {
                radioButtonOneTime.setChecked(true);
            }
        }
    }

    private void showDatePickerDialog() {
        if (!textViewDate.isEnabled()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        if (!selectedDate.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = dateFormat.parse(selectedDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedCalendar.set(year1, month1, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = dateFormat.format(selectedCalendar.getTime());

                    textViewDate.setText(formatDisplayDate(selectedDate));
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        if (!textViewTime.isEnabled()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        if (!selectedTime.isEmpty()) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date time = timeFormat.parse(selectedTime);
                if (time != null) {
                    calendar.setTime(time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute1);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    selectedTime = timeFormat.format(selectedCalendar.getTime());

                    textViewTime.setText(selectedTime);
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private String formatDisplayDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    private void saveEvent() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, getString(R.string.event_fill_title), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.event_fill_date), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.event_fill_time), Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarEvent event;

        if (isEditMode && existingEvent != null) {
            existingEvent.setTitle(title);
            existingEvent.setDescription(description);
            existingEvent.setDate(selectedDate);
            existingEvent.setTime(selectedTime);
            existingEvent.setNotificationType(selectedNotificationType);
            if (existingEvent.getEventId() == null || existingEvent.getEventId().isEmpty()) {
                existingEvent.setEventId(IdGenerator.generateUUID());
            }
            event = existingEvent;
        } else {
            event = new CalendarEvent(title, description, selectedDate, selectedTime,
                    firebaseManager.isUserLoggedIn() ? firebaseManager.getUserId() : "");
            event.setNotificationType(selectedNotificationType);
            if (event.getEventId() == null || event.getEventId().isEmpty()) {
                event.setEventId(IdGenerator.generateUUID());
            }
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("event", event);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
