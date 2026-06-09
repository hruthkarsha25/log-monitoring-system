package com.project.logmonitoringsystem.specification;

import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.model.LogEvent;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class LogSpecification {

    public static Specification<LogEvent> hasLevel(LogLevel level) {
        return (root, query, cb) ->
                cb.equal(root.get("level"), level);
    }

    public static Specification<LogEvent> hasEndpoint(String endpoint) {
        return (root, query, cb) ->
                cb.equal(root.get("endpoint"), endpoint);
    }

    public static Specification<LogEvent> hasUsername(String username) {
        return (root, query, cb) ->
                cb.equal(root.get("username"), username);
    }

    public static Specification<LogEvent> hasServiceName(String serviceName) {
        return (root, query, cb) ->
                cb.equal(root.get("serviceName"), serviceName);
    }

    public static Specification<LogEvent> createdAfter(LocalDateTime startDate) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<LogEvent> createdBefore(LocalDateTime endDate) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }
}
