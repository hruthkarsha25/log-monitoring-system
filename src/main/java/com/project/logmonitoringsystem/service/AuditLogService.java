package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.model.AuditLog;
import com.project.logmonitoringsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String username, String endpoint, String email, String method) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .email(email)
                .endpoint(endpoint)
                .method(method)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
