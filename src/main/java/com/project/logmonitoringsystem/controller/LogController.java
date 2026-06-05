package com.project.logmonitoringsystem.controller;

import com.project.logmonitoringsystem.dto.LogStatsDTO;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.kafka.LogProducer;
import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogProducer logProducer;
    private final LogService logService;
    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    public LogController(LogProducer logProducer, LogService logService) {
        this.logProducer = logProducer;
        this.logService = logService;
    }

    @PostMapping
    public String sendLog(@RequestBody LogEvent logEvent, HttpServletRequest request) {
        log.info("API_LOG endpoint=/logs method=POST");
        try {
            if (logEvent.getEndpoint() == null) {
                logEvent.setEndpoint(request.getRequestURI());
            }

            if (logEvent.getMethod() == null) {
                logEvent.setMethod(request.getMethod());
            }

            if (logEvent.getUsername() == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    logEvent.setUsername(authentication.getName());
                }
            }

            logProducer.sendlog(logEvent);
            log.info("LOG_SENT_SUCCESS endpoint={} method={} username={}",
                logEvent.getEndpoint(), logEvent.getMethod(), logEvent.getUsername());
            return "Log sent to Kafka!";
        } catch (Exception e) {
            log.error("LOG_SEND_FAILED exception={} message={}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/all")
    public Page<LogEvent> getAllLogs(@PageableDefault(
            sort = "createdAt",
            direction = Sort.Direction.DESC
    ) Pageable pageable) {
        log.info("API_LOG endpoint=/logs/all method=GET page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<LogEvent> result = logService.getAllLogs(pageable);
            log.info("DB_QUERY executed table=log_events totalElements={} pageNumber={}", result.getTotalElements(), pageable.getPageNumber());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/level")
    public List<LogEvent> getByLevel(@RequestParam String level) {
        log.info("API_LOG endpoint=/logs/level method=GET level={}", level);
        try {
            List<LogEvent> result = logService.getByLevel(level);
            log.info("DB_QUERY executed table=log_events filter=level level={} count={}", level, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=level exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/service")
    public List<LogEvent> getByService(@RequestParam String service) {
        log.info("API_LOG endpoint=/logs/service method=GET service={}", service);
        try {
            List<LogEvent> result = logService.getByService(service);
            log.info("DB_QUERY executed table=log_events filter=service service={} count={}", service, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=service exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/filter")
    public List<LogEvent> getByLevelAndService(@RequestParam String level, @RequestParam String service) {
        log.info("API_LOG endpoint=/logs/filter method=GET level={} service={}", level, service);
        try {
            List<LogEvent> result = logService.getByLevelAndService(level, service);
            log.info("DB_QUERY executed table=log_events filter=level_and_service level={} service={} count={}", level, service, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=level_and_service exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/time")
    public List<LogEvent> getByTime(@RequestParam String start, @RequestParam String end) {
        log.info("API_LOG endpoint=/logs/time method=GET start={} end={}", start, end);
        try {
            List<LogEvent> result = logService.getByTimeRange(start, end);
            log.info("DB_QUERY executed table=log_events filter=time_range start={} end={} count={}", start, end, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=time_range exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/count")
    public Map<String, Long> getLogCount() {
        log.info("API_LOG endpoint=/logs/count method=GET");
        try {
            Map<String, Long> result = logService.getLogCounts();
            log.info("DB_QUERY executed table=log_events operation=count_by_level result_size={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events operation=count_by_level exception={}", e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Get log statistics",
            description = "Returns dashboard statistics for logs"
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public LogStatsDTO getLogStats() {
        log.info("API_LOG endpoint=/logs/stats method=GET authorization=ADMIN_USER");
        try {
            LogStatsDTO result = logService.getLogStats();
            log.info("STATS_RETRIEVED totalLogs={} infoLogs={} warnLogs={} errorLogs={}",
                result.getTotalLogs(), result.getInfoLogs(), result.getWarnLogs(), result.getErrorLogs());
            return result;
        } catch (Exception e) {
            log.error("STATS_RETRIEVAL_FAILED exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/user/{username}")
    public List<LogEvent> getLogsByUser(@PathVariable String username) {
        log.info("API_LOG endpoint=/logs/user/{username} method=GET username={}", username);
        try {
            List<LogEvent> result = logService.getLogsByUsernameLogsOnly(username);
            log.info("DB_QUERY executed table=log_events filter=username username={} count={}", username, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=username exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/endpoint")
    public List<LogEvent> getByEndpoint(@RequestParam String path) {
        log.info("API_LOG endpoint=/logs/endpoint method=GET path={}", path);
        try {
            List<LogEvent> result = logService.getLogsByEndpoint(path);
            log.info("DB_QUERY executed table=log_events filter=endpoint path={} count={}", path, result.size());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events filter=endpoint exception={}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/search")
    public Page<LogEvent> searchLogs(

            @RequestParam(required = false) LogLevel level,

            @RequestParam(required = false) String endpoint,

            @RequestParam(required = false) String username,

            @RequestParam(required = false) String serviceName,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            Pageable pageable
    ) {
        log.info("API_LOG endpoint=/logs/search method=GET level={} endpoint={} username={} serviceName={} page={}",
            level, endpoint, username, serviceName, pageable.getPageNumber());
        try {
            Page<LogEvent> result = logService.searchLogs(
                    level,
                    endpoint,
                    username,
                    serviceName,
                    startDate,
                    endDate,
                    pageable
            );
            log.info("DB_QUERY executed table=log_events operation=search_with_filters results={}", result.getTotalElements());
            return result;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=log_events operation=search_with_filters exception={}", e.getMessage());
            throw e;
        }
    }
}
