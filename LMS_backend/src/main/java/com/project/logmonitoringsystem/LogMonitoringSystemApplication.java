package com.project.logmonitoringsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.project.logmonitoringsystem"})
@EntityScan("com.project.logmonitoringsystem")
public class LogMonitoringSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogMonitoringSystemApplication.class, args);
    }

}
