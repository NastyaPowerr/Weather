package org.roadmap.weather.util;

public final class ValidationConstants {
    public static final String MISSING_LOGIN = "Login is required.";
    public static final String MISSING_PASSWORD = "Password is required.";

    public static final String INVALID_LATITUDE_RANGE = "Latitude must be between -90 and 90.";
    public static final String INVALID_LONGITUDE_RANGE = "Longitude must be between -180 and 180.";

    private ValidationConstants() {
    }
}
