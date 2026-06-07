package com.project.logmonitoringsystem.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.project.logmonitoringsystem.dto.AuditEvent;
import com.project.logmonitoringsystem.model.AuditLog;
import com.project.logmonitoringsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuditConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ElasticsearchClient elasticsearchClient;
    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    @KafkaListener(
            topics = "audit-events",
            groupId = "audit-group",
            containerFactory = "auditKafkaListenerContainerFactory"
    )
    public void consume(AuditEvent event) {
        log.info("AUDIT_EVENT_RECEIVED endpoint={} method={} username={}",
                event.getEndpoint(), event.getMethod(), event.getUsername());

        AuditLog auditLog = AuditLog.builder()
                .username(event.getUsername())
                .email(event.getEmail())
                .endpoint(event.getEndpoint())
                .method(event.getMethod())
                .timestamp(event.getTimestamp())
                .build();

        auditLogRepository.save(auditLog);
        log.info("AUDIT_LOG_SAVED endpoint={}", event.getEndpoint());

        try {
            elasticsearchClient.index(i -> i
                    .index("audit-index")
                    .document(event)
            );
            log.info("AUDIT_SENT_TO_ELASTICSEARCH endpoint={}", event.getEndpoint());
        } catch (Exception e) {
            log.error("AUDIT_ELASTICSEARCH_FAILED reason={}", e.getMessage(), e); // ← full stack trace now
        }
    }
}