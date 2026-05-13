package org.roadmap.weather.exception.location;

public class DuplicateLocationException extends RuntimeException {
    public DuplicateLocationException(String message) {
        super(message);
    }
}
