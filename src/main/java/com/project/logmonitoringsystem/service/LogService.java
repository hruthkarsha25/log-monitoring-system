package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.dto.LogStatsDTO;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.repository.LogRepository;
import com.project.logmonitoringsystem.specification.LogSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    public Page<LogEvent> getAllLogs(Pageable pageable) {
        log.info("LOG_SERVICE operation=getAllLogs page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        try {
            log.info("DB_QUERY executed table=log_events operation=fetch_paginated page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());
            return logRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events operation=fetch_paginated exception={}", e.getMessage());
            throw e;
        }
    }

    public List<LogEvent> getByLevel(String level) {
        log.info("LOG_SERVICE operation=getByLevel level={}", level);
        try {
            LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
            log.info("DB_QUERY executed table=log_events filter=level level={}", logLevel);
            return logRepository.findByLevel(logLevel);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=level exception={}", e.getMessage());
            throw e;
        }
    }

    public List<LogEvent> getByService(String service) {
        log.info("LOG_SERVICE operation=getByService service={}", service);
        try {
            log.info("DB_QUERY executed table=log_events filter=service_name service={}", service);
            return logRepository.findByServiceName(service);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=service_name exception={}", e.getMessage());
            throw e;
        }
    }

    public List<LogEvent> getByLevelAndService(String level, String service) {
        log.info("LOG_SERVICE operation=getByLevelAndService level={} service={}", level, service);
        try {
            LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
            log.info("DB_QUERY executed table=log_events filter=level_and_service level={} service={}", logLevel, service);
            return logRepository.findByLevelAndServiceName(logLevel, service);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=level_and_service exception={}", e.getMessage());
            throw e;
        }
    }

    public List<LogEvent> getByTimeRange(String start, String end) {
        log.info("LOG_SERVICE operation=getByTimeRange start={} end={}", start, end);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
            log.info("DB_QUERY executed table=log_events filter=time_range start={} end={}", startDateTime, endDateTime);
            return logRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=time_range exception={}", e.getMessage());
            throw e;
        }
    }

    public Map<String, Long> getLogCounts() {
        log.info("LOG_SERVICE operation=getLogCounts");
        try {
            log.info("DB_QUERY executed table=log_events operation=count_by_level");
            List<Object[]> results = logRepository.countLogsByLevel();

            Map<String, Long> counts = new HashMap<>();

            for(Object[] row : results) {
                counts.put(String.valueOf(row[0]), (Long) row[1]);
            }
            log.info("LOG_COUNTS_RETRIEVED levels={}", counts.keySet());
            return counts;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events operation=count_by_level exception={}", e.getMessage());
            throw e;
        }
    }

    public LogStatsDTO getLogStats() {
        log.info("LOG_SERVICE operation=getLogStats");
        try {
            log.info("DB_QUERY executed table=log_events operation=count");
            long totalLogs = logRepository.count();

            long infoLogs = 0;
            long warnLogs = 0;
            long errorLogs = 0;

            log.info("DB_QUERY executed table=log_events operation=count_by_level");
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

            log.info("DB_QUERY executed table=log_events operation=count_by_date date=today");
            long logsToday =
                    logRepository.countByCreatedAtAfter(startOfDay);

            LocalDateTime last24Hours =
                    LocalDateTime.now().minusHours(24);

            log.info("DB_QUERY executed table=log_events operation=count_by_date time_range=last_24_hours");
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

            log.info("DB_QUERY executed table=log_events operation=count_by_service");
            List<Object[]> serviceResults =
                    logRepository.countLogsByService();

            for (Object[] row : serviceResults) {
                logsByService.put(
                        String.valueOf(row[0]),
                        (Long) row[1]
                );
            }

            log.info("LOG_STATS_GENERATED totalLogs={} infoLogs={} warnLogs={} errorLogs={} logsToday={} logsLast24Hours={}",
                totalLogs, infoLogs, warnLogs, errorLogs, logsToday, logsLast24Hours);

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
        } catch (Exception e) {
            log.error("LOG_STATS_GENERATION_FAILED exception={}", e.getMessage());
            throw e;
        }
    }

    public List<LogEvent> getLogsByUsernameLogsOnly(String username) {
        log.info("LOG_SERVICE operation=getLogsByUsernameLogsOnly username={}", username);
        try {
            log.info("DB_QUERY executed table=log_events filter=username username={}", username);
            return logRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=username exception={}", e.getMessage());
            throw e;
        }
    }

    public List<LogEvent> getLogsByEndpoint(String path) {
        log.info("LOG_SERVICE operation=getLogsByEndpoint path={}", path);
        try {
            log.info("DB_QUERY executed table=log_events filter=endpoint path={}", path);
            return logRepository.findByEndpoint(path);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=endpoint exception={}", e.getMessage());
            throw e;
        }
    }

    public Page<LogEvent> searchLogs(
            LogLevel level,
            String endpoint,
            String username,
            String serviceName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.info("LOG_SERVICE operation=searchLogs level={} endpoint={} username={} serviceName={} page={}",
            level, endpoint, username, serviceName, pageable.getPageNumber());
        try {
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

            log.info("DB_QUERY executed table=log_events operation=search_with_specification page={}", pageable.getPageNumber());
            Page<LogEvent> result = logRepository.findAll(spec, pageable);
            log.info("SEARCH_RESULTS_RETRIEVED totalElements={} pageNumber={}", result.getTotalElements(), pageable.getPageNumber());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events operation=search_with_specification exception={}", e.getMessage());
            throw e;
        }
    }
}
