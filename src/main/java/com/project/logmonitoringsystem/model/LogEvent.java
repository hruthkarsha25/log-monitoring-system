package com.project.logmonitoringsystem.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.logmonitoringsystem.enums.LogLevel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Data
public class LogEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String serviceName;

    @Enumerated(EnumType.STRING)
    private LogLevel level;

    private String message;

    private String endpoint;

    private String method;

    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void setcreatedAt() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
