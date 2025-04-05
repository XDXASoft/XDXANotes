package ru.xdxasoft.xdxanotes.utils.notes;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.Models.CalendarEvent;
import ru.xdxasoft.xdxanotes.utils.IdGenerator;

public class CalendarEventTakerActivity extends AppCompatActivity {

    private static final String TAG = "CalendarEventTaker";

    private EditText editTextTitle, editTextDescription;
    private TextView textViewDate, textViewTime, textViewNotificationTime;
    private Button buttonSave;
    private ImageView imageBack;
    private RadioGroup radioGroupNotificationType;
    private RadioButton radioButtonNoNotification, radioButtonOneTime, radioButtonAllDay;

    private String selectedDate = "";
    private String selectedTime = "";
    private String selectedNotificationTime = "";
    private int selectedNotificationType = 0; // 0 - нет, 1 - одноразовое, 2 - весь день
    private boolean isEditMode = false;
    private CalendarEvent existingEvent;
    private FirebaseManager firebaseManager;
    private Calendar selectedCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_event_taker);

        initViews();
        setupListeners();

        firebaseManager = FirebaseManager.getInstance(this);
        selectedCalendar = Calendar.getInstance();

        // По умолчанию показываем опцию "Без уведомления"
        radioButtonNoNotification.setChecked(true);
        textViewNotificationTime.setVisibility(View.GONE);
        textViewNotificationTime.setText("Напомнить за 15 минут");

        // Проверяем, находимся ли мы в режиме редактирования
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

                // Устанавливаем значения для уведомлений
                selectedNotificationType = existingEvent.getNotificationType();
                selectedNotificationTime = existingEvent.getNotificationTime();

                // Выбираем соответствующий радиобаттон
                if (selectedNotificationType == 0) {
                    radioButtonNoNotification.setChecked(true);
                    textViewNotificationTime.setVisibility(View.GONE);
                } else if (selectedNotificationType == 1) {
                    radioButtonOneTime.setChecked(true);
                    textViewNotificationTime.setVisibility(View.VISIBLE);
                    if (!selectedNotificationTime.isEmpty()) {
                        textViewNotificationTime.setText("Напомнить в " + selectedNotificationTime);
                    }
                } else if (selectedNotificationType == 2) {
                    radioButtonAllDay.setChecked(true);
                    textViewNotificationTime.setVisibility(View.GONE);
                }

                // Проверяем, не является ли событие прошедшим
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar eventCalendar = Calendar.getInstance();
                    Date eventDate = dateFormat.parse(selectedDate);
                    if (eventDate != null) {
                        eventCalendar.setTime(eventDate);

                        // Сброс времени для сравнения только дат
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
                            // Если событие в прошлом, запрещаем менять дату и время
                            textViewDate.setEnabled(false);
                            textViewTime.setEnabled(false);

                            // Визуальное отображение отключенных полей
                            textViewDate.setAlpha(0.5f);
                            textViewTime.setAlpha(0.5f);

                            // Показываем предупреждение
                            ToastManager.showToast(
                                    this,
                                    "Событие в прошлом. Нельзя изменить дату и время.",
                                    R.drawable.ic_error,
                                    ContextCompat.getColor(this, R.color.warning_yellow),
                                    ContextCompat.getColor(this, R.color.black),
                                    ContextCompat.getColor(this, R.color.black),
                                    true
                            );
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (getIntent().hasExtra("date")) {
            // Если это создание нового события, и передана дата
            selectedDate = getIntent().getStringExtra("date");
            textViewDate.setText(formatDisplayDate(selectedDate));

            // Проверяем, не является ли событие прошедшим
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar eventCalendar = Calendar.getInstance();
                Date eventDate = dateFormat.parse(selectedDate);
                if (eventDate != null) {
                    eventCalendar.setTime(eventDate);

                    // Сброс времени для сравнения только дат
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
                        // При создании события в прошлом, показываем уведомление
                        ToastManager.showToast(
                                this,
                                "Создание события в прошлом. Установите статус 'Выполнено'.",
                                R.drawable.ic_error,
                                ContextCompat.getColor(this, R.color.warning_yellow),
                                ContextCompat.getColor(this, R.color.black),
                                ContextCompat.getColor(this, R.color.black),
                                true
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // По умолчанию текущая дата
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(calendar.getTime());
            textViewDate.setText(formatDisplayDate(selectedDate));

            // По умолчанию текущее время (округленное до ближайших 30 минут)
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            minute = (minute / 30) * 30; // Округляем до ближайших 30 минут
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
        textViewNotificationTime = findViewById(R.id.textViewNotificationTime);
        buttonSave = findViewById(R.id.buttonSaveEvent);
        imageBack = findViewById(R.id.imageBackEvent);

        radioGroupNotificationType = findViewById(R.id.radioGroupNotificationType);
        radioButtonNoNotification = findViewById(R.id.radioButtonNoNotification);
        radioButtonOneTime = findViewById(R.id.radioButtonOneTime);
        radioButtonAllDay = findViewById(R.id.radioButtonAllDay);
    }

    private void setupListeners() {
        imageBack.setOnClickListener(v -> onBackPressed());

        textViewDate.setOnClickListener(v -> showDatePickerDialog());
        textViewTime.setOnClickListener(v -> showTimePickerDialog());
        textViewNotificationTime.setOnClickListener(v -> showNotificationTimePickerDialog());

        radioGroupNotificationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonNoNotification) {
                selectedNotificationType = 0;
                textViewNotificationTime.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioButtonOneTime) {
                selectedNotificationType = 1;
                textViewNotificationTime.setVisibility(View.VISIBLE);

                // Если время уведомления не задано, устанавливаем время события минус 15 минут
                if (selectedNotificationTime.isEmpty() && !selectedTime.isEmpty()) {
                    try {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        Date eventTime = timeFormat.parse(selectedTime);
                        if (eventTime != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(eventTime);
                            calendar.add(Calendar.MINUTE, -15);
                            selectedNotificationTime = timeFormat.format(calendar.getTime());
                            textViewNotificationTime.setText("Напомнить в " + selectedNotificationTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (checkedId == R.id.radioButtonAllDay) {
                selectedNotificationType = 2;
                textViewNotificationTime.setVisibility(View.GONE);
            }
        });

        buttonSave.setOnClickListener(v -> saveEvent());
    }

    private void showDatePickerDialog() {
        // Если поле заблокировано для прошедшего события, не показываем диалог
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
        // Если поле заблокировано для прошедшего события, не показываем диалог
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

    private void showNotificationTimePickerDialog() {
        if (selectedNotificationType != 1) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        if (!selectedNotificationTime.isEmpty()) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date time = timeFormat.parse(selectedNotificationTime);
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
                    selectedNotificationTime = timeFormat.format(selectedCalendar.getTime());

                    textViewNotificationTime.setText("Напомнить в " + selectedNotificationTime);
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
            ToastManager.showToast(
                    this,
                    "Введите название события",
                    R.drawable.ic_error,
                    ContextCompat.getColor(this, R.color.error_red),
                    ContextCompat.getColor(this, R.color.black),
                    ContextCompat.getColor(this, R.color.black),
                    true
            );
            return;
        }

        if (selectedDate.isEmpty()) {
            ToastManager.showToast(
                    this,
                    "Выберите дату",
                    R.drawable.ic_error,
                    ContextCompat.getColor(this, R.color.error_red),
                    ContextCompat.getColor(this, R.color.black),
                    ContextCompat.getColor(this, R.color.black),
                    true
            );
            return;
        }

        if (selectedTime.isEmpty()) {
            ToastManager.showToast(
                    this,
                    "Выберите время",
                    R.drawable.ic_error,
                    ContextCompat.getColor(this, R.color.error_red),
                    ContextCompat.getColor(this, R.color.black),
                    ContextCompat.getColor(this, R.color.black),
                    true
            );
            return;
        }

        CalendarEvent event;

        if (isEditMode && existingEvent != null) {
            // Редактируем существующее событие
            existingEvent.setTitle(title);
            existingEvent.setDescription(description);
            existingEvent.setDate(selectedDate);
            existingEvent.setTime(selectedTime);
            existingEvent.setNotificationType(selectedNotificationType);
            existingEvent.setNotificationTime(selectedNotificationTime);
            // Убеждаемся, что у события есть eventId
            if (existingEvent.getEventId() == null || existingEvent.getEventId().isEmpty()) {
                existingEvent.setEventId(IdGenerator.generateUUID());
            }
            event = existingEvent;
        } else {
            // Создаем новое событие
            event = new CalendarEvent(title, description, selectedDate, selectedTime,
                    firebaseManager.isUserLoggedIn() ? firebaseManager.getUserId() : "");
            event.setNotificationType(selectedNotificationType);
            event.setNotificationTime(selectedNotificationTime);
            // У нового события eventId должен быть установлен в конструкторе
            // Но на всякий случай проверим
            if (event.getEventId() == null || event.getEventId().isEmpty()) {
                event.setEventId(IdGenerator.generateUUID());
            }
        }

        Intent intent = new Intent();
        intent.putExtra("event", event);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
