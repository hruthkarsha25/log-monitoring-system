package com.project.logmonitoringsystem.kafka;

import com.project.logmonitoringsystem.model.LogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogProducer {

    private final KafkaTemplate<String, LogEvent> kafkatemplate;

    public LogProducer(KafkaTemplate<String, LogEvent> kafkatemplate) {
        this.kafkatemplate = kafkatemplate;
    }

    public void sendlog(LogEvent logEvent) {
        kafkatemplate.send("log-events", logEvent);
    }
}
