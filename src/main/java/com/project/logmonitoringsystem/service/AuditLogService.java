package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.model.AuditLog;
import com.project.logmonitoringsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    public void logAction(String username, String endpoint, String email, String method) {
        log.info("AUDIT_LOG_ATTEMPT username={} endpoint={} email={} method={}", username, endpoint, email, method);
        try {
            LocalDateTime now = LocalDateTime.now();
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .email(email)
                    .endpoint(endpoint)
                    .method(method)
                    .timestamp(now)
                    .build();

            log.info("DB_QUERY executed table=audit_log operation=insert username={} endpoint={} method={}", username, endpoint, method);
            auditLogRepository.save(auditLog);
            log.info("AUDIT_LOG_CREATED username={} endpoint={} email={} method={} timestamp={}",
                username, endpoint, email, method, now);
        } catch (Exception e) {
            log.error("AUDIT_LOG_FAILED username={} endpoint={} exception={}", username, endpoint, e.getMessage());
            throw e;
        }
    }

    public List<AuditLog> getAllAuditLogs() {
        log.info("AUDIT_LOG_FETCH operation=fetch_all");
        try {
            log.info("DB_QUERY executed table=audit_log operation=fetch_all");
            List<AuditLog> result = auditLogRepository.findAll();
            log.info("AUDIT_LOGS_RETRIEVED count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=audit_log operation=fetch_all exception={}", e.getMessage());
            throw e;
        }
    }
}
