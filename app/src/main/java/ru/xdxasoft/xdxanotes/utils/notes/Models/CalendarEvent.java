package ru.xdxasoft.xdxanotes.utils.notes.Models;

import androidx.annotation.Keep;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import ru.xdxasoft.xdxanotes.utils.IdGenerator;

@Keep
@Entity(tableName = "calendar_events")
public class CalendarEvent implements Serializable {

    @PrimaryKey(autoGenerate = true)
    int ID = 0;

    @ColumnInfo(name = "eventId")
    String eventId = "";

    @ColumnInfo(name = "title")
    String title = "";

    @ColumnInfo(name = "description")
    String description = "";

    @ColumnInfo(name = "date")
    String date = ""; // Формат: yyyy-MM-dd

    @ColumnInfo(name = "time")
    String time = ""; // Формат: HH:mm

    @ColumnInfo(name = "completed")
    boolean completed = false;

    @ColumnInfo(name = "userId")
    String userId = "";

    @ColumnInfo(name = "lastModified")
    long lastModified = 0;

    @ColumnInfo(name = "notificationType")
    int notificationType = 0; // 0 - нет уведомления, 1 - одноразовое, 2 - весь день

    @ColumnInfo(name = "notificationTime")
    String notificationTime = ""; // Время уведомления, если отличается от времени события

    public CalendarEvent() {
        this.lastModified = System.currentTimeMillis();
        this.eventId = IdGenerator.generateComplexId();
    }

    public CalendarEvent(String title, String description, String date, String time, String userId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.userId = userId;
        this.lastModified = System.currentTimeMillis();
        this.eventId = IdGenerator.generateComplexId();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getEventId() {
        return eventId != null ? eventId : "";
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.lastModified = System.currentTimeMillis();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lastModified = System.currentTimeMillis();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        this.lastModified = System.currentTimeMillis();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.lastModified = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
        this.lastModified = System.currentTimeMillis();
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
        this.lastModified = System.currentTimeMillis();
    }
}
