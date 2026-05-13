package org.roadmap.weather.scheduler;

import org.roadmap.weather.service.SessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionCleaner {
    private final SessionService sessionService;

    public SessionCleaner(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void cleanExpiredSessions() {
        sessionService.deleteExpiredSessions();
    }
}
