package com.project.logmonitoringsystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.logmonitoringsystem.model.LogEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JacksonConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public JsonSerializer<LogEvent> jsonSerializer(ObjectMapper objectMapper) {
        return new JsonSerializer<>(objectMapper);
    }

    @Bean
    public ProducerFactory<String, LogEvent> producerFactory(JsonSerializer<LogEvent> jsonSerializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", bootstrapServers);
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("value.serializer", JsonSerializer.class);

        DefaultKafkaProducerFactory<String, LogEvent> factory = new DefaultKafkaProducerFactory<>(configProps);
        factory.setValueSerializer(jsonSerializer);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, LogEvent> kafkaTemplate(ProducerFactory<String, LogEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
