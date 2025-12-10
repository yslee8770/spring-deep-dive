package com.example.spring_deep_dive.exception;

public class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }
}
