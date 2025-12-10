package com.example.spring_deep_dive.exception;

public class OrderValidationException extends Exception {
    public OrderValidationException(String message) {
        super(message);
    }
}
