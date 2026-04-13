package org.roadmap.weather.exception.location;

public class GeocodingApiCallException extends RuntimeException {
    public GeocodingApiCallException(String message) {
        super(message);
    }
}
