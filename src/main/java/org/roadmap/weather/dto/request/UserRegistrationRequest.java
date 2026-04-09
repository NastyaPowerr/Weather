package org.roadmap.weather.dto.request;

public record UserRegistrationRequest(
        String username,
        String password,
        String repeatedPassword
) {
}