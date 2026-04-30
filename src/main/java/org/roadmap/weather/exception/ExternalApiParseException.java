package org.roadmap.weather.exception;

public class ExternalApiParseException extends RuntimeException {
    public ExternalApiParseException(String message) {
        super(message);
    }
}
