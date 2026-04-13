package org.roadmap.weather.exception.location;

public class LocationAlreadyExistsForUserException extends RuntimeException {
    public LocationAlreadyExistsForUserException(String message) {
        super(message);
    }
}
