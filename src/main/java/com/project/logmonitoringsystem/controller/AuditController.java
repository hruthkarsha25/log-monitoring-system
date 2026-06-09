package com.project.logmonitoringsystem.controller;

import com.project.logmonitoringsystem.model.AuditLog;
import com.project.logmonitoringsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public List<AuditLog> getAuditLogs() {
        log.info("API_LOG endpoint=/audit/logs method=GET authorization=ADMIN");
        try {
            List<AuditLog> result = auditLogRepository.findAll();
            log.info("DB_QUERY executed table=audit_logs operation=fetch_all count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=audit_logs operation=fetch_all exception={}", e.getMessage());
            throw e;
        }
    }
}
