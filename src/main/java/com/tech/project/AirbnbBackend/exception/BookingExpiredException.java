package com.tech.project.AirbnbBackend.exception;

public class BookingExpiredException extends RuntimeException {
    public BookingExpiredException(String message) {
        super(message);
    }
}

