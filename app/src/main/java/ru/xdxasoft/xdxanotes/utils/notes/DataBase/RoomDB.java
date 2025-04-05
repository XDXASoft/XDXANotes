package ru.xdxasoft.xdxanotes.utils.notes.DataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ru.xdxasoft.xdxanotes.utils.notes.Models.CalendarEvent;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;

@Database(entities = {Notes.class, CalendarEvent.class}, version = 7, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {

    private static RoomDB database;
    private static String DATABASE_NAME = "NoteApp";

    public synchronized static RoomDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract MainDAO mainDao();

    public abstract CalendarDao calendarDao();
}
