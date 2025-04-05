package ru.xdxasoft.xdxanotes.utils.notes.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ru.xdxasoft.xdxanotes.utils.notes.Models.CalendarEvent;

@Dao
public interface CalendarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalendarEvent event);

    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    List<CalendarEvent> getAll();

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY CASE WHEN notificationType = 2 THEN 0 ELSE 1 END, time ASC")
    List<CalendarEvent> getEventsByDate(String date);

    @Query("SELECT * FROM calendar_events WHERE ID = :id")
    CalendarEvent getById(int id);

    @Query("SELECT * FROM calendar_events WHERE eventId = :eventId")
    CalendarEvent getByEventId(String eventId);

    @Query("UPDATE calendar_events SET completed = :completed WHERE ID = :id")
    void updateCompletionStatus(int id, boolean completed);

    @Query("UPDATE calendar_events SET completed = :completed WHERE eventId = :eventId")
    void updateCompletionStatusByEventId(String eventId, boolean completed);

    @Query("UPDATE calendar_events SET title = :title, description = :description, date = :date, time = :time WHERE ID = :id")
    void update(int id, String title, String description, String date, String time);

    @Query("UPDATE calendar_events SET title = :title, description = :description, date = :date, time = :time WHERE eventId = :eventId")
    void updateByEventId(String eventId, String title, String description, String date, String time);

    @Delete
    void delete(CalendarEvent event);

    @Query("DELETE FROM calendar_events WHERE eventId = :eventId")
    void deleteByEventId(String eventId);

    @Query("DELETE FROM calendar_events")
    void deleteAll();
}
