package ru.xdxasoft.xdxanotes.models;

import androidx.annotation.Keep;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

import ru.xdxasoft.xdxanotes.utils.IdGenerator;

@Keep
public class Password implements Serializable {

    @PropertyName("id")
    private String id;

    @PropertyName("title")
    private String title;

    @PropertyName("username")
    private String username;

    @PropertyName("password")
    private String password;

    @PropertyName("userId")
    private String userId;

    public Password() {

    }

    public Password(String id, String title, String username, String password, String userId) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.userId = userId;
    }

    public Password(String title, String username, String password, String userId) {
        this.id = IdGenerator.generateRandomId();
        this.title = title;
        this.username = username;
        this.password = password;
        this.userId = userId;
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

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
