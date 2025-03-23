package com.example.electronicstore.model;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String time;
    private boolean isRead;
    private String userId;

    public Notification() {
    }

    public Notification(String id, String title, String message, String time, boolean isRead, String userId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.isRead = isRead;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
