package com.project.logmonitoringsystem.alert;

import com.project.logmonitoringsystem.alert.entity.Alert;
import com.project.logmonitoringsystem.enums.AlertStatus;
import com.project.logmonitoringsystem.enums.AlertType;
import com.project.logmonitoringsystem.enums.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;


    public void createAlert(String type, String message, String endpoint, String severity) {

        Alert alert = new Alert();
        alert.setType(AlertType.valueOf(type));
        alert.setMessage(message);
        alert.setEndpoint(endpoint);
        alert.setSeverity(Severity.valueOf(severity));
        alert.setStatus(AlertStatus.OPEN);
        alert.setTimestamp(LocalDateTime.now());


        Optional<Alert> existingAlert =
                alertRepository.findByTypeAndEndpointAndStatus(
                        type,
                        endpoint,
                        AlertStatus.OPEN
                );

        if (existingAlert.isPresent()) {
            return;
        }
    }

    public List<Alert> findAllAlerts() {
        return alertRepository.findAll();
    }

    public List<Alert> severityAlerts(String severity) {
        return alertRepository.findAll()
                .stream()
                .filter(a -> a.getSeverity().name().equalsIgnoreCase(severity))
                .toList();
    }

    public List<Alert> alertType(String type) {
        return alertRepository.findAll()
                .stream()
                .filter(a -> a.getType().name().equalsIgnoreCase(type))
                .toList();
    }

    public List<Alert> latestAlerts() {
        return alertRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(20)
                .toList();
    }

    public Alert alertAcknowledge(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(AlertStatus.ACKNOWLEDGED);

        return alertRepository.save(alert);
    }

    public Alert resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    public List<Alert> openAlerts() {
        return alertRepository.findByStatus(AlertStatus.OPEN);
    }
}
