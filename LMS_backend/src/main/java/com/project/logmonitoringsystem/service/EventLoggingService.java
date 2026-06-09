package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.kafka.LogProducer;
import com.project.logmonitoringsystem.model.LogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class EventLoggingService {

    private final LogProducer logProducer;

    public void log(String serviceName, LogLevel level, String message, String endpoint, String method, String username) {

        LogEvent event = new LogEvent();

        event.setServiceName(serviceName);
        event.setLevel(level);
        event.setMessage(message);
        event.setEndpoint(endpoint);
        event.setMethod(method);
        event.setUsername(username);
        event.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        logProducer.sendlog(event);
    }
}
