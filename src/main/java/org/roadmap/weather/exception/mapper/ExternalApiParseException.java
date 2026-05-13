package org.roadmap.weather.exception.mapper;

public class ExternalApiParseException extends RuntimeException {
    public ExternalApiParseException(String message) {
        super(message);
    }
}
