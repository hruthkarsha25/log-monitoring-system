package com.project.logmonitoringsystem.alert.controller;

import com.project.logmonitoringsystem.alert.AlertRepository;
import com.project.logmonitoringsystem.alert.AlertService;
import com.project.logmonitoringsystem.alert.entity.Alert;
import com.project.logmonitoringsystem.enums.AlertStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertService.findAllAlerts();
    }

    @GetMapping("/severity/{severity}")
    public List<Alert> getBySeverity(@PathVariable String severity) {
        return alertService.severityAlerts(severity);
    }

    @GetMapping("/type/{type}")
    public List<Alert> getByType(@PathVariable String type) {
        return alertService.alertType(type);
    }

    @GetMapping("/latest")
    public List<Alert> getLatest() {
        return alertService.latestAlerts();
    }

    @PutMapping("/{id}/acknowledge")
    public Alert acknowledge(@PathVariable Long id) {

        Alert alert = alertService.alertAcknowledge(id);

        return ResponseEntity.ok(alert).getBody();
    }

    @PutMapping("/{id}/resolve")
    public Alert resolve(@PathVariable Long id) {

        Alert alert = alertService.resolveAlert(id);
        return ResponseEntity.ok(alert).getBody();
    }

    @GetMapping("/open")
    public List<Alert> getOpenAlerts() {
        return alertService.openAlerts();
    }

}
