package com.project.logmonitoringsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogEventDTO {
    private String serviceName;
    private String level;
    private String message;
    private LocalDateTime timestamp;
}
