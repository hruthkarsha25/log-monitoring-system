package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.dto.LogStatsDTO;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.repository.LogRepository;
import com.project.logmonitoringsystem.specification.LogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public Page<LogEvent> getAllLogs(Pageable pageable) {
        return logRepository.findAll(pageable);
    }

    public List<LogEvent> getByLevel(String level) {
        LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
       return logRepository.findByLevel(logLevel);
    }

    public List<LogEvent> getByService(String service) {
        return logRepository.findByServiceName(service);
    }

    public List<LogEvent> getByLevelAndService(String level, String service) {
        LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
        return logRepository.findByLevelAndServiceName(logLevel, service);
    }

    public List<LogEvent> getByTimeRange(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
        return logRepository.findByCreatedAtBetween(startDateTime, endDateTime);
    }

    public Map<String, Long> getLogCounts() {
        List<Object[]> results = logRepository.countLogsByLevel();

        Map<String, Long> counts = new HashMap<>();

        for(Object[] row : results) {
            counts.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return counts;
    }

    public LogStatsDTO getLogStats() {

        long totalLogs = logRepository.count();

        long infoLogs = 0;
        long warnLogs = 0;
        long errorLogs = 0;

        List<Object[]> results = logRepository.countLogsByLevel();

        for(Object[] row : results) {
            String level = String.valueOf(row[0]);
            long count = (Long) row[1];

            switch (level) {
                case "INFO":
                    infoLogs = count;
                    break;

                case "WARN":
                    warnLogs = count;
                    break;

                case "ERROR":
                    errorLogs = count;
                    break;
            }
        }

        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        long logsToday =
                logRepository.countByCreatedAtAfter(startOfDay);

        LocalDateTime last24Hours =
                LocalDateTime.now().minusHours(24);

        long logsLast24Hours =
                logRepository.countByCreatedAtAfter(last24Hours);

        Map<String, Long> logsByLevel = new HashMap<>();

        for (Object[] row : results) {
            logsByLevel.put(
                    String.valueOf(row[0]),
                    (Long) row[1]
            );
        }

        Map<String, Long> logsByService = new HashMap<>();

        List<Object[]> serviceResults =
                logRepository.countLogsByService();

        for (Object[] row : serviceResults) {
            logsByService.put(
                    String.valueOf(row[0]),
                    (Long) row[1]
            );
        }

        return new LogStatsDTO(
                totalLogs,
                infoLogs,
                warnLogs,
                errorLogs,
                logsToday,
                logsLast24Hours,
                logsByService,
                logsByLevel
        );
    }

    public Optional<User> getLogsByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<LogEvent> getLogsByEndpoint(String path) {
        return userRepository.findByEndpoint(path);
    }

    public Page<LogEvent> searchLogs(
            LogLevel level,
            String endpoint,
            String username,
            String serviceName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Specification<LogEvent> spec = Specification.where(null);

        if(level != null) {
            spec = spec.and(LogSpecification.hasLevel(level));
        }

        if(endpoint != null) {
            spec = spec.and(LogSpecification.hasEndpoint(endpoint));
        }

        if(username != null) {
            spec = spec.and(LogSpecification.hasUsername(username));
        }

        if(serviceName != null) {
            spec = spec.and(LogSpecification.hasServiceName(serviceName));
        }

        if(startDate != null) {
            spec = spec.and(LogSpecification.createdAfter(startDate));
        }

        if(endDate != null) {
            spec = spec.and(LogSpecification.createdBefore(endDate));
        }

        return logRepository.findAll(spec, pageable);
    }
}
