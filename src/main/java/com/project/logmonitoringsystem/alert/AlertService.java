package com.project.logmonitoringsystem.alert;

import com.project.logmonitoringsystem.alert.entity.Alert;
import com.project.logmonitoringsystem.enums.AlertStatus;
import com.project.logmonitoringsystem.enums.AlertType;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.enums.Severity;
import com.project.logmonitoringsystem.service.EventLoggingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private final EventLoggingService eventLoggingService;

    public void createAlert(AlertType type, String message, String endpoint, Severity severity) {
        log.info("ALERT_CREATE_ATTEMPT type={} endpoint={} severity={}", type, endpoint, severity);
        try {
            Optional<Alert> existingAlert =
                    alertRepository.findByTypeAndEndpointAndStatus(
                            type,
                            endpoint,
                            AlertStatus.OPEN
                    );

            if (existingAlert.isPresent()) {
                log.warn("ALERT_DUPLICATE type={} endpoint={} alertId={}", type, endpoint, existingAlert.get().getId());
                return;
            }

            Alert alert = new Alert();
            alert.setType(type);
            alert.setMessage(message);
            alert.setEndpoint(endpoint);
            alert.setSeverity(severity);
            alert.setStatus(AlertStatus.OPEN);
            alert.setTimestamp(LocalDateTime.now());

            log.info("DB_QUERY executed table=alerts operation=insert type={} endpoint={}", type, endpoint);
            alertRepository.save(alert);

            eventLoggingService.log(
                    "alert-service",
                    LogLevel.WARN,
                    "ALERT_CREATED",
                    endpoint,
                    null,
                    null
            );
            log.info("ALERT_CREATED type={} endpoint={} severity={} status=OPEN", type, endpoint, severity);
        } catch (Exception e) {

            eventLoggingService.log(
                    "alert-service",
                    LogLevel.ERROR,
                    "ALERT_CREATE_FAILED",
                    null,
                    null,
                    null
            );
            log.error("ALERT_CREATE_FAILED type={} endpoint={} exception={}", type, endpoint, e.getMessage());
            throw e;
        }
    }

    public List<Alert> findAllAlerts() {
        log.info("ALERT_FETCH_ALL operation=fetch_all");
        try {
            log.info("DB_QUERY executed table=alerts operation=fetch_all");
            List<Alert> result = alertRepository.findAll();
            log.info("ALERTS_RETRIEVED count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts operation=fetch_all exception={}", e.getMessage());
            throw e;
        }
    }

    public List<Alert> severityAlerts(String severity) {
        log.info("ALERT_FETCH_BY_SEVERITY severity={}", severity);
        try {
            List<Alert> result = alertRepository.findAll()
                    .stream()
                    .filter(a -> a.getSeverity().name().equalsIgnoreCase(severity))
                    .toList();
            log.info("DB_QUERY executed table=alerts filter=severity severity={} count={}", severity, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=severity exception={}", e.getMessage());
            throw e;
        }
    }

    public List<Alert> alertType(String type) {
        log.info("ALERT_FETCH_BY_TYPE type={}", type);
        try {
            List<Alert> result = alertRepository.findAll()
                    .stream()
                    .filter(a -> a.getType().name().equalsIgnoreCase(type))
                    .toList();
            log.info("DB_QUERY executed table=alerts filter=type type={} count={}", type, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=type exception={}", e.getMessage());
            throw e;
        }
    }

    public List<Alert> latestAlerts() {
        log.info("ALERT_FETCH_LATEST operation=fetch_latest");
        try {
            List<Alert> result = alertRepository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .limit(20)
                    .toList();
            log.info("DB_QUERY executed table=alerts operation=fetch_latest limit=20 count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts operation=fetch_latest exception={}", e.getMessage());
            throw e;
        }
    }

    public Alert alertAcknowledge(Long id) {
        log.info("ALERT_ACKNOWLEDGE_ATTEMPT alertId={}", id);
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = null;

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {

            username = authentication.getName();
        }

        try {
            log.info("DB_QUERY executed table=alerts operation=select_by_id alertId={}", id);
            Alert alert = alertRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("ALERT_NOT_FOUND alertId={}", id);
                        return new RuntimeException("Alert not found");
                    });
            alert.setStatus(AlertStatus.ACKNOWLEDGED);

            log.info("DB_QUERY executed table=alerts operation=update alertId={} newStatus=ACKNOWLEDGED", id);
            Alert result = alertRepository.save(alert);

            eventLoggingService.log(
                    "alert-service",
                    LogLevel.INFO,
                    "ALERT_ACKNOWLEDGED",
                    alert.getEndpoint(),
                    null,
                    username
            );
            log.info("ALERT_ACKNOWLEDGED alertId={} previousStatus=OPEN newStatus=ACKNOWLEDGED", id);
            return result;
        } catch (Exception e) {
            eventLoggingService.log(
                    "alert-service",
                    LogLevel.ERROR,
                    "ALERT_ACKNOWLEDGE_FAILED",
                    "/alerts/{id}/acknowledge",
                    null,
                    username
            );
            log.error("ALERT_ACKNOWLEDGE_FAILED alertId={} exception={}", id, e.getMessage());
            throw e;
        }
    }

    public Alert resolveAlert(Long id) {
        log.info("ALERT_RESOLVE_ATTEMPT alertId={}", id);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = null;

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {

            username = authentication.getName();
        }

        try {
            log.info("DB_QUERY executed table=alerts operation=select_by_id alertId={}", id);
            Alert alert = alertRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("ALERT_NOT_FOUND alertId={}", id);
                        return new RuntimeException("Alert not found");
                    });

            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(LocalDateTime.now());

            log.info("DB_QUERY executed table=alerts operation=update alertId={} newStatus=RESOLVED", id);
            Alert result = alertRepository.save(alert);

            eventLoggingService.log(
                    "alert-service",
                    LogLevel.INFO,
                    "ALERT_RESOLVED",
                    alert.getEndpoint(),
                    null,
                    null
            );
            log.info("ALERT_RESOLVED alertId={} previousStatus=ACKNOWLEDGED newStatus=RESOLVED", id);
            return result;
        } catch (Exception e) {

            eventLoggingService.log(
                    "alert-service",
                    LogLevel.ERROR,
                    "ALERT_RESOLVE_FAILED",
                    "/alerts/{id}/resolve",
                    null,
                    username
            );
            log.error("ALERT_RESOLVE_FAILED alertId={} exception={}", id, e.getMessage());
            throw e;
        }
    }

    public List<Alert> openAlerts() {
        log.info("ALERT_FETCH_OPEN operation=fetch_open");
        try {
            log.info("DB_QUERY executed table=alerts filter=status status=OPEN");
            List<Alert> result = alertRepository.findByStatus(AlertStatus.OPEN);
            log.info("ALERTS_RETRIEVED status=OPEN count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=status exception={}", e.getMessage());
            throw e;
        }
    }

    public List<Alert> acknowledgeAlerts() {
        log.info("ALERT_FETCH_ACKNOWLEDGED operation=fetch_acknowledged");
        try {
            log.info("DB_QUERY executed table=alerts filter=status status=ACKNOWLEDGED");
            List<Alert> result = alertRepository.findByStatus(AlertStatus.ACKNOWLEDGED);
            log.info("ALERTS_RETRIEVED status=ACKNOWLEDGED count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=status exception={}", e.getMessage());
            throw e;
        }
    }

    public List<Alert> resolvedAlerts() {
        log.info("ALERT_FETCH_RESOLVED operation=fetch_resolved");
        try {
            log.info("DB_QUERY executed table=alerts filter=status status=RESOLVED");
            List<Alert> result = alertRepository.findByStatus(AlertStatus.RESOLVED);
            log.info("ALERTS_RETRIEVED status=RESOLVED count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=status exception={}", e.getMessage());
            throw e;
        }
    }
}
