package org.roadmap.weather.exception.user;

public class InvalidUserParamsException extends RuntimeException {
    public InvalidUserParamsException(String message) {
        super(message);
    }
}
