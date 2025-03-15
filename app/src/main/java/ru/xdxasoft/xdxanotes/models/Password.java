package ru.xdxasoft.xdxanotes.models;

import androidx.annotation.Keep;

import java.io.Serializable;

import ru.xdxasoft.xdxanotes.utils.IdGenerator;

@Keep // Аннотация для ProGuard, чтобы не удалял этот класс
public class Password implements Serializable {

    private String id;
    private String title;
    private String username;
    private String password;

    // Пустой конструктор для Firebase - ОБЯЗАТЕЛЬНО должен быть!
    public Password() {
        // Пустой конструктор
    }

    public Password(String id, String title, String username, String password) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
    }

    // Конструктор для создания нового пароля с автоматической генерацией ID
    public Password(String title, String username, String password) {
        this.id = IdGenerator.generateRandomId();
        this.title = title;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password != null ? password : "";
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
