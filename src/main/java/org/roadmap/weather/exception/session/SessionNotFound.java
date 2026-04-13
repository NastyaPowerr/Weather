package org.roadmap.weather.exception.session;

public class SessionNotFound extends RuntimeException {
    public SessionNotFound(String message) {
        super(message);
    }
}
