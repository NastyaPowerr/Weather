package org.roadmap.weather.exception;

public class GeocodingApiCallException extends RuntimeException {
    public GeocodingApiCallException(String message) {
        super(message);
    }
}
