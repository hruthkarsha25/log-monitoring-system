package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.alert.AlertService;
import com.project.logmonitoringsystem.enums.AlertType;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.enums.Severity;
import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(LogConsumer.class);

    @Value("${app.service.name}")
    private String serviceName;

    @KafkaListener(topics = "log-events", groupId = "log-group")
    public void consume(LogEvent logEvent) {
        log.info("KAFKA_MESSAGE_RECEIVED topic=log-events level={} endpoint={} service={}",
            logEvent.getLevel(), logEvent.getEndpoint(), logEvent.getServiceName());
        try {
            if (logEvent.getServiceName() == null) {
                logEvent.setServiceName(serviceName);
                log.info("SERVICE_NAME_SET serviceName={}", serviceName);
            }

            log.info("DB_QUERY executed table=log_events operation=insert level={} endpoint={}",
                logEvent.getLevel(), logEvent.getEndpoint());
            logRepository.save(logEvent);
            log.info("LOG_EVENT_PERSISTED logEventId={} level={} endpoint={}",
                logEvent.getId(), logEvent.getLevel(), logEvent.getEndpoint());

            handleErrorSpike(logEvent);
            handleLoginFailures(logEvent);
            handleEndpointTraffic(logEvent);
        } catch (Exception e) {
            log.error("KAFKA_MESSAGE_PROCESSING_FAILED endpoint={} exception={} reason={}",
                logEvent.getEndpoint(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private void handleErrorSpike(LogEvent logEvent) {
        log.info("ERROR_SPIKE_CHECK endpoint={} level={}", logEvent.getEndpoint(), logEvent.getLevel());
        try {
            if (logEvent.getLevel() != LogLevel.ERROR) {
                return;
            }

            String endpoint = logEvent.getEndpoint();

            String aggregationKey;
            if (endpoint != null && !endpoint.isBlank()) {
                aggregationKey = endpoint;
            } else if (logEvent.getServiceName() != null && !logEvent.getServiceName().isBlank()) {
                aggregationKey = "service:" + logEvent.getServiceName();
            } else {
                aggregationKey = "GLOBAL";
            }

            LocalDateTime now = LocalDateTime.now();

            List<LocalDateTime> events =
                    errorEvents.computeIfAbsent(
                            aggregationKey,
                            k -> new ArrayList<>());

            events.add(now);

            events.removeIf(
                    time -> time.isBefore(now.minusMinutes(1)));

            log.info("ERROR_EVENTS_TRACKED aggregationKey={} eventCount={}", aggregationKey, events.size());

            if (events.size() >= 10) {

                String alertEndpoint = (endpoint != null && !endpoint.isBlank()) ? endpoint : (
                        logEvent.getServiceName() != null ? ("service:" + logEvent.getServiceName()) : "GLOBAL"
                );

                log.warn("ERROR_SPIKE_DETECTED aggregationKey={} errorCount={} threshold=10", aggregationKey, events.size());

                alertService.createAlert(
                        AlertType.ERROR_SPIKE,
                        "10+ errors detected within 1 minute",
                        alertEndpoint,
                        Severity.HIGH
                );

                events.clear();
            }
        } catch (Exception e) {
            log.error("ERROR_SPIKE_HANDLING_FAILED endpoint={} exception={}",
                logEvent.getEndpoint(), e.getMessage());
            throw e;
        }
    }

    private void handleLoginFailures(LogEvent logEvent) {
        log.info("LOGIN_FAILURE_CHECK endpoint={} message={}", logEvent.getEndpoint(), logEvent.getMessage());
        try {
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

            log.info("LOGIN_FAILURE_EVENTS_TRACKED username={} failureCount={}", username, events.size());

            if (events.size() >= 5) {

                log.warn("LOGIN_FAILURE_SPIKE_DETECTED username={} failureCount={} threshold=5 timeWindow=5_minutes",
                    username, events.size());

                alertService.createAlert(
                        AlertType.LOGIN_FAILURE,
                        "5 failed login attempts within 5 minutes for user " + username,
                        logEvent.getEndpoint(),
                        Severity.MEDIUM
                );

                events.clear();
            }
        } catch (Exception e) {
            log.error("LOGIN_FAILURE_HANDLING_FAILED username={} exception={}",
                logEvent.getUsername(), e.getMessage());
            throw e;
        }
    }

    private void handleEndpointTraffic(LogEvent logEvent) {
        log.info("ENDPOINT_TRAFFIC_CHECK endpoint={}", logEvent.getEndpoint());
        try {
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

            log.info("ENDPOINT_TRAFFIC_EVENTS_TRACKED endpoint={} requestCount={}", endpoint, events.size());

            if (events.size() >= 20) {

                log.warn("TRAFFIC_SPIKE_DETECTED endpoint={} requestCount={} threshold=20 timeWindow=1_minute",
                    endpoint, events.size());

                alertService.createAlert(
                        AlertType.TRAFFIC_SPIKE,
                        "20+ requests detected within 1 minute",
                        endpoint,
                        Severity.LOW
                );

                events.clear();
            }
        } catch (Exception e) {
            log.error("ENDPOINT_TRAFFIC_HANDLING_FAILED endpoint={} exception={}",
                logEvent.getEndpoint(), e.getMessage());
            throw e;
        }
    }
}
