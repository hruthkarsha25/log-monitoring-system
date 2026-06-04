package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.alert.AlertService;
import com.project.logmonitoringsystem.enums.AlertType;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.enums.Severity;
import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LogConsumer {

    private final AlertService alertService;

    private final Map<String, List<LocalDateTime>> errorEvents =
            new ConcurrentHashMap<>();

    private final Map<String, List<LocalDateTime>> loginFailureEvents =
            new ConcurrentHashMap<>();

    private final Map<String, List<LocalDateTime>> endpointTrafficEvents =
            new ConcurrentHashMap<>();

    private final LogRepository logRepository;

    @Value("${app.service.name}")
    private String serviceName;

    @KafkaListener(topics = "log-events", groupId = "log-group")
    public void consume(LogEvent logEvent) {

        if(logEvent.getServiceName() == null) {
            logEvent.setServiceName(serviceName);
        }
        logRepository.save(logEvent);
        System.out.println("Received Log: " + logEvent);

        handleErrorSpike(logEvent);
        handleLoginFailures(logEvent);
        handleEndpointTraffic(logEvent);
    }

    private void handleErrorSpike(LogEvent logEvent) {

        if (logEvent.getLevel() != LogLevel.ERROR) {
            return;
        }

        String endpoint = logEvent.getEndpoint();

        if (endpoint == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<LocalDateTime> events =
                errorEvents.computeIfAbsent(
                        endpoint,
                        k -> new ArrayList<>());

        events.add(now);

        events.removeIf(
                time -> time.isBefore(now.minusMinutes(1)));

        if (events.size() >= 10) {

            alertService.createAlert(
                    AlertType.ERROR_SPIKE.name(),
                    "10+ errors detected within 1 minute",
                    endpoint,
                    Severity.HIGH.name()
            );

            events.clear();
        }
    }

    private void handleLoginFailures(LogEvent logEvent) {

        if (logEvent.getEndpoint() == null ||
                logEvent.getMessage() == null ||
                !logEvent.getEndpoint().contains("/auth/login") ||
                !logEvent.getMessage().contains("FAILED")) {
            return;
        }

        String username = logEvent.getUsername();

        if (username == null) {
            username = "UNKNOWN";
        }

        LocalDateTime now = LocalDateTime.now();

        List<LocalDateTime> events =
                loginFailureEvents.computeIfAbsent(
                        username,
                        k -> new ArrayList<>());

        events.add(now);

        events.removeIf(
                time -> time.isBefore(now.minusMinutes(5)));

        if (events.size() >= 5) {

            alertService.createAlert(
                    AlertType.LOGIN_FAILURE.name(),
                    "5 failed login attempts within 5 minutes for user " + username,
                    logEvent.getEndpoint(),
                    Severity.MEDIUM.name()
            );

            events.clear();
        }
    }
    private void handleEndpointTraffic(LogEvent logEvent) {

        String endpoint = logEvent.getEndpoint();

        if (endpoint == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<LocalDateTime> events =
                endpointTrafficEvents.computeIfAbsent(
                        endpoint,
                        k -> new ArrayList<>());

        events.add(now);

        events.removeIf(
                time -> time.isBefore(now.minusMinutes(1)));

        if (events.size() >= 20) {

            alertService.createAlert(
                    AlertType.TRAFFIC_SPIKE.name(),
                    "20+ requests detected within 1 minute",
                    endpoint,
                    Severity.LOW.name()
            );

            events.clear();
        }
    }
}
