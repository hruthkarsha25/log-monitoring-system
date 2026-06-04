package com.project.logmonitoringsystem.controller;

import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.dto.LogStatsDTO;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.kafka.LogProducer;
import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogProducer logProducer;
    private final LogService logService;

    public LogController(LogProducer logProducer, LogService logService) {
        this.logProducer = logProducer;
        this.logService = logService;
    }

    @PostMapping
    public String sendLog(@RequestBody LogEvent logEvent) {
        logProducer.sendlog(logEvent);
        return "Log sent to Kafka!";
    }

    @GetMapping("/all")
    public Page<LogEvent> getAllLogs(@PageableDefault(
            size = 10,
            sort = "createdAt",
            direction = Sort.Direction.DESC
    ) Pageable pageable) {
        return logService.getAllLogs(pageable);
    }

    @GetMapping("/level")
    public List<LogEvent> getByLevel(@RequestParam String level) {
        return logService.getByLevel(level);
    }

    @GetMapping("/service")
    public List<LogEvent> getByService(@RequestParam String service) {
        return logService.getByService(service);
    }

    @GetMapping("/search")
    public List<LogEvent> getByLevelAndService(@RequestParam String level, @RequestParam String service) {
        return logService.getByLevelAndService(level, service);
    }

    @GetMapping("/time")
    public List<LogEvent> getByTime(@RequestParam String start, @RequestParam String end) {
        return logService.getByTimeRange(start, end);
    }

    @GetMapping("/count")
    public Map<String, Long> getLogCount() {
        return logService.getLogCounts();
    }

    @Operation(
            summary = "Get log statistics",
            description = "Returns dashboard statistics for logs"
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public LogStatsDTO getLogStats() {
        return logService.getLogStats();
    }

    @GetMapping("/user/{username}")
    public Optional<User> getLogsByUser(@PathVariable String username) {
        return logService.getLogsByUsername(username);
    }

    @GetMapping("/endpoint")
    public List<LogEvent> getByEndpoint(@RequestParam String path) {
        return logService.getLogsByEndpoint(path);
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

        return logService.searchLogs(
                level,
                endpoint,
                username,
                serviceName,
                startDate,
                endDate,
                pageable
        );
    }
}
