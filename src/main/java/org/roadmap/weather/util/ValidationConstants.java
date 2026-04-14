package org.roadmap.weather.util;

public final class ValidationConstants {
    public static final int MIN_LOGIN_LENGTH = 2;
    public static final int MAX_LOGIN_LENGTH = 20;
    public static final String LOGIN_PATTERN = "^[A-Za-zА-Яа-я-.@0-9]+$";
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;

    public static final String MISSING_LOGIN = "Login is required.";
    public static final String INVALID_LOGIN_LENGTH = "Login length must be between {min} and {max} characters.";
    public static final String INVALID_LOGIN_PATTERN =
            "Login can contain only: English and Russian letters, dots, commercial at.";
    public static final String MISSING_PASSWORD = "Password is required.";
    public static final String MISSING_REPEATED_PASSWORD = "Repeat your password.";
    public static final String INVALID_PASSWORD_LENGTH = "Password length must be between {min} and {max} characters.";

    private ValidationConstants() {
    }
}
