package com.project.logmonitoringsystem.alert.controller;

import com.project.logmonitoringsystem.alert.AlertService;
import com.project.logmonitoringsystem.alert.entity.Alert;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    private static final Logger log = LoggerFactory.getLogger(AlertController.class);

    @GetMapping
    public List<Alert> getAllAlerts() {
        log.info("API_LOG endpoint=/alerts method=GET");
        try {
            List<Alert> result = alertService.findAllAlerts();
            log.info("DB_QUERY executed table=alerts operation=fetch_all count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts operation=fetch_all exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/severity/{severity}")
    public List<Alert> getBySeverity(@PathVariable String severity) {
        log.info("API_LOG endpoint=/alerts/severity/{severity} method=GET severity={}", severity);
        try {
            List<Alert> result = alertService.severityAlerts(severity);
            log.info("DB_QUERY executed table=alerts filter=severity severity={} count={}", severity, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=severity exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/type/{type}")
    public List<Alert> getByType(@PathVariable String type) {
        log.info("API_LOG endpoint=/alerts/type/{type} method=GET type={}", type);
        try {
            List<Alert> result = alertService.alertType(type);
            log.info("DB_QUERY executed table=alerts filter=type type={} count={}", type, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=type exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/latest")
    public List<Alert> getLatest() {
        log.info("API_LOG endpoint=/alerts/latest method=GET");
        try {
            List<Alert> result = alertService.latestAlerts();
            log.info("DB_QUERY executed table=alerts operation=fetch_latest limit=20 count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts operation=fetch_latest exception={}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}/acknowledge")
    public Alert acknowledge(@PathVariable Long id) {
        log.info("API_LOG endpoint=/alerts/{id}/acknowledge method=PUT alertId={}", id);
        try {
            Alert alert = alertService.alertAcknowledge(id);
            log.info("ALERT_ACKNOWLEDGED alertId={} previousStatus=OPEN newStatus=ACKNOWLEDGED", id);
            return ResponseEntity.ok(alert).getBody();
        } catch (Exception e) {
            log.error("ALERT_ACKNOWLEDGE_FAILED alertId={} exception={}", id, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}/resolve")
    public Alert resolve(@PathVariable Long id) {
        log.info("API_LOG endpoint=/alerts/{id}/resolve method=PUT alertId={}", id);
        try {
            Alert alert = alertService.resolveAlert(id);
            log.info("ALERT_RESOLVED alertId={} previousStatus=ACKNOWLEDGED newStatus=RESOLVED", id);
            return ResponseEntity.ok(alert).getBody();
        } catch (Exception e) {
            log.error("ALERT_RESOLVE_FAILED alertId={} exception={}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/open")
    public List<Alert> getOpenAlerts() {
        log.info("API_LOG endpoint=/alerts/open method=GET");
        try {
            List<Alert> result = alertService.openAlerts();
            log.info("DB_QUERY executed table=alerts filter=status status=OPEN count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=status exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/acknowledged")
    public List<Alert> getAcknowledgedAlerts(){
        log.info("API_LOG endpoint=/alerts/acknowledged method=GET");
        try {
            List<Alert> result = alertService.acknowledgeAlerts();
            log.info("DB_QUERY executed table=alerts filter=status status=ACKNOWLEDGED count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=alerts filter=status exception={}", e.getMessage());
            throw e;
        }
    }


}
