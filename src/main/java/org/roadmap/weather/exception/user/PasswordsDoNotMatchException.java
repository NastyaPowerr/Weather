package org.roadmap.weather.exception.user;

import org.roadmap.weather.exception.ValidationException;

public class PasswordsDoNotMatchException extends ValidationException {
    public PasswordsDoNotMatchException(String message) {
        super(message);
    }
}
