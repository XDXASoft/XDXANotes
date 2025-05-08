package ru.xdxasoft.xdxanotes.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.CalendarEventTakerActivity;
import ru.xdxasoft.xdxanotes.utils.notes.DataBase.RoomDB;
import ru.xdxasoft.xdxanotes.utils.notes.Models.CalendarEvent;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link CalendarFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class CalendarFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "CalendarFragment";
    private static final int REQUEST_CODE_ADD_EVENT = 101;
    private static final int REQUEST_CODE_UPDATE_EVENT = 102;

    private View rootView;
    private TextView textViewMonthYear;
    private LinearLayout daysContainer;
    private RecyclerView recyclerViewEvents;
    private FloatingActionButton fabAddEvent;

    private Calendar currentCalendar;
    private Calendar selectedDateCalendar;
    private List<CalendarEvent> allEvents;
    private List<CalendarEvent> selectedDateEvents;
    private EventsAdapter eventsAdapter;

    private RoomDB database;
    private FirebaseManager firebaseManager;

    private Map<String, List<CalendarEvent>> eventsByDateMap;
    private List<CardView> dateCards;



    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        currentCalendar = Calendar.getInstance();
        selectedDateCalendar = Calendar.getInstance();
        database = RoomDB.getInstance(getActivity());
        firebaseManager = FirebaseManager.getInstance(getActivity());
        allEvents = new ArrayList<>();
        selectedDateEvents = new ArrayList<>();
        eventsByDateMap = new HashMap<>();
        dateCards = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        initViews();
        setupCalendar();
        loadEvents();
        setupListeners();
        return rootView;
    }

    private void initViews() {
        textViewMonthYear = rootView.findViewById(R.id.textViewMonthYear);
        daysContainer = rootView.findViewById(R.id.daysContainer);
        recyclerViewEvents = rootView.findViewById(R.id.recyclerViewEvents);
        fabAddEvent = rootView.findViewById(R.id.fabAddEvent);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventsAdapter = new EventsAdapter(selectedDateEvents);
        recyclerViewEvents.setAdapter(eventsAdapter);
    }

    private void setupListeners() {
        rootView.findViewById(R.id.imageButtonPrevMonth).setOnClickListener(this);
        rootView.findViewById(R.id.imageButtonNextMonth).setOnClickListener(this);
        fabAddEvent.setOnClickListener(this);
    }

    private void setupCalendar() {
        updateMonthYearText();
        generateCalendarDays();
    }

    private void updateMonthYearText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));
        textViewMonthYear.setText(sdf.format(currentCalendar.getTime()));
    }

    private void generateCalendarDays() {
        dateCards.clear();
        daysContainer.removeAllViews();

        Date currentDate = currentCalendar.getTime();

        Calendar calendar = (Calendar) currentCalendar.clone();

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (firstDayOfWeek == Calendar.SUNDAY) {
            firstDayOfWeek = 7;
        } else {
            firstDayOfWeek -= 1;
        }

        calendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1));

        for (int week = 0; week < 6; week++) {
            LinearLayout weekRow = new LinearLayout(getActivity());
            weekRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            weekRow.setOrientation(LinearLayout.HORIZONTAL);
            weekRow.setWeightSum(7);

            for (int day = 0; day < 7; day++) {
                CardView dayCard = createDayView(calendar);
                weekRow.addView(dayCard);
                dateCards.add(dayCard);

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            daysContainer.addView(weekRow);

            if (calendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH) && week >= 3) {
                break;
            }
        }

        currentCalendar.setTime(currentDate);
    }

    private CardView createDayView(Calendar calendar) {
        boolean isCurrentMonth = calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH);
        boolean isToday = isDateToday(calendar);
        boolean isSelected = isDateSelected(calendar);

        CardView cardView = new CardView(getActivity());
        int cardSize = getResources().getDimensionPixelSize(R.dimen.day_card_size);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                0,
                cardSize);
        cardParams.weight = 1;
        cardParams.setMargins(2, 5, 2, 5);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(getResources().getDimensionPixelSize(R.dimen.day_card_radius));
        cardView.setCardElevation(getResources().getDimensionPixelSize(R.dimen.day_card_elevation));

        if (isSelected) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.selected_day));
        } else if (isToday) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.today));
        } else if (isCurrentMonth) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.current_month_day));
        } else {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.other_month_day));
        }

        TextView textViewDay = new TextView(getActivity());
        textViewDay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        textViewDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        textViewDay.setTextSize(16);
        textViewDay.setGravity(android.view.Gravity.CENTER);

        if (isCurrentMonth) {
            textViewDay.setTextColor(getResources().getColor(R.color.white));
        } else {
            textViewDay.setTextColor(getResources().getColor(R.color.gray_text));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(calendar.getTime());
        if (eventsByDateMap.containsKey(dateString) && !eventsByDateMap.get(dateString).isEmpty()) {

            LinearLayout dayLayout = new LinearLayout(getActivity());
            dayLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setGravity(android.view.Gravity.CENTER);

            dayLayout.addView(textViewDay);

            View indicator = new View(getActivity());
            LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                    8, 8
            );
            indicatorParams.topMargin = 4;
            indicator.setLayoutParams(indicatorParams);
            indicator.setBackgroundResource(R.drawable.event_indicator);
            dayLayout.addView(indicator);

            cardView.addView(dayLayout);
        } else {
            cardView.addView(textViewDay);
        }

        cardView.setTag(dateString);

        cardView.setOnClickListener(v -> {
            try {
                Date date = dateFormat.parse((String) v.getTag());
                if (date != null) {
                    selectedDateCalendar.setTime(date);
                    updateSelectedDate();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        return cardView;
    }

    private boolean isDateToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isDateSelected(Calendar calendar) {
        return calendar.get(Calendar.YEAR) == selectedDateCalendar.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == selectedDateCalendar.get(Calendar.MONTH)
                && calendar.get(Calendar.DAY_OF_MONTH) == selectedDateCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private void updateSelectedDate() {
        for (CardView cardView : dateCards) {
            String dateString = (String) cardView.getTag();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = dateFormat.parse(dateString);
                Calendar cardCalendar = Calendar.getInstance();
                if (date != null) {
                    cardCalendar.setTime(date);
                    if (isDateSelected(cardCalendar)) {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.selected_day));
                    } else if (isDateToday(cardCalendar)) {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.today));
                    } else if (cardCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.current_month_day));
                    } else {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.other_month_day));
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        loadEventsForSelectedDate();
    }

    private void loadEvents() {
        allEvents.clear();
        eventsByDateMap.clear();

        allEvents.addAll(database.calendarDao().getAll());

        for (CalendarEvent event : allEvents) {
            String date = event.getDate();
            if (!eventsByDateMap.containsKey(date)) {
                eventsByDateMap.put(date, new ArrayList<>());
            }
            eventsByDateMap.get(date).add(event);
        }

        setupCalendar();

        loadEventsForSelectedDate();
    }

    private void loadEventsForSelectedDate() {
        selectedDateEvents.clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateString = dateFormat.format(selectedDateCalendar.getTime());

        if (eventsByDateMap.containsKey(selectedDateString)) {
            selectedDateEvents.addAll(eventsByDateMap.get(selectedDateString));

            selectedDateEvents.sort((event1, event2) -> {
                if (event1.getNotificationType() == 2 && event2.getNotificationType() != 2) {
                    return -1;
                } else if (event1.getNotificationType() != 2 && event2.getNotificationType() == 2) {
                    return 1;
                } else {
                    return event1.getTime().compareTo(event2.getTime());
                }
            });
        }

        TextView todayHeader = rootView.findViewById(R.id.textViewTodayEvents);
        if (isDateToday(selectedDateCalendar)) {
            todayHeader.setText("Сегодня");
        } else {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM", new Locale("ru"));
            todayHeader.setText(displayFormat.format(selectedDateCalendar.getTime()));
        }

        eventsAdapter.notifyDataSetChanged();

        View emptyView = rootView.findViewById(R.id.emptyEventsView);
        if (selectedDateEvents.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewEvents.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerViewEvents.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.imageButtonPrevMonth) {
            currentCalendar.add(Calendar.MONTH, -1);
            setupCalendar();
        } else if (id == R.id.imageButtonNextMonth) {
            currentCalendar.add(Calendar.MONTH, 1);
            setupCalendar();
        } else if (id == R.id.fabAddEvent) {
            Intent intent = new Intent(getActivity(), CalendarEventTakerActivity.class);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            intent.putExtra("date", dateFormat.format(selectedDateCalendar.getTime()));
            startActivityForResult(intent, REQUEST_CODE_ADD_EVENT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_ADD_EVENT || requestCode == REQUEST_CODE_UPDATE_EVENT) {
                CalendarEvent event = (CalendarEvent) data.getSerializableExtra("event");
                if (event != null) {

                    if (requestCode == REQUEST_CODE_ADD_EVENT) {
                        database.calendarDao().insert(event);

                        if (firebaseManager.isUserLoggedIn()) {
                            firebaseManager.saveCalendarEventToFirebase(event, success -> {
                                if (success) {
                                    showToast(getString(R.string.note_saved));
                                } else {
                                    showToast(getString(R.string.note_saved));
                                }
                            });
                        } else {
                            showToast(getString(R.string.note_saved));
                        }
                    } else {
                        database.calendarDao().update(
                                event.getID(),
                                event.getTitle(),
                                event.getDescription(),
                                event.getDate(),
                                event.getTime()
                        );

                        if (firebaseManager.isUserLoggedIn()) {
                            firebaseManager.saveCalendarEventToFirebase(event, success -> {
                                if (success) {
                                    showToast(getString(R.string.note_saved));
                                } else {
                                    showToast(getString(R.string.note_saved));
                                }
                            });
                        } else {
                            showToast(getString(R.string.note_saved));
                        }
                    }

                    loadEvents();
                }
            }
        }
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            ToastManager.showToast(
                    getActivity(),
                    message,
                    R.drawable.ic_galohca_black,
                    getResources().getColor(R.color.success_green),
                    getResources().getColor(R.color.black),
                    getResources().getColor(R.color.black),
                    false
            );
        }
    }

    private class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

        private List<CalendarEvent> events;

        public EventsAdapter(List<CalendarEvent> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_event, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            CalendarEvent event = events.get(position);

            if (event.getNotificationType() == 2) {
                holder.textViewTime.setText("Весь день");
                holder.textViewTime.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.textViewTime.setText(event.getTime());
                holder.textViewTime.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            holder.textViewTitle.setText(event.getTitle());

            if (event.isCompleted()) {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.completed_event));
                holder.imageViewComplete.setImageResource(R.drawable.ic_check_completed);
            } else {
                if (event.getNotificationType() == 2) {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.selected_day));
                } else {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.current_month_day));
                }
                holder.imageViewComplete.setImageResource(R.drawable.ic_check);
            }

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CalendarEventTakerActivity.class);
                intent.putExtra("event", event);
                startActivityForResult(intent, REQUEST_CODE_UPDATE_EVENT);
            });

            holder.imageViewComplete.setOnClickListener(v -> {
                boolean newStatus = !event.isCompleted();
                event.setCompleted(newStatus);

                database.calendarDao().updateCompletionStatus(event.getID(), newStatus);

                if (firebaseManager.isUserLoggedIn()) {
                    firebaseManager.saveCalendarEventToFirebase(event, null);
                }

                notifyItemChanged(position);
            });

            holder.cardView.setOnLongClickListener(v -> {
                database.calendarDao().delete(event);

                if (firebaseManager.isUserLoggedIn()) {
                    firebaseManager.deleteCalendarEventFromFirebase(event, null);
                }

                loadEvents();

                showToast("Событие удалено");
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class EventViewHolder extends RecyclerView.ViewHolder {

            CardView cardView;
            TextView textViewTime;
            TextView textViewTitle;
            ImageView imageViewComplete;

            public EventViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardViewEvent);
                textViewTime = itemView.findViewById(R.id.textViewEventTime);
                textViewTitle = itemView.findViewById(R.id.textViewEventTitle);
                imageViewComplete = itemView.findViewById(R.id.imageViewComplete);
            }
        }
    }
}
