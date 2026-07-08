package org.roadmap.weather.scheduler;

import lombok.RequiredArgsConstructor;
import org.roadmap.weather.service.impl.SessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionCleaner {
    private final SessionService sessionService;

    @Scheduled(cron = "0 0 10 * * *")
    public void cleanExpiredSessions() {
        sessionService.deleteExpiredSessions();
    }
}
