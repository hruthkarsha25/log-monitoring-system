package com.project.logmonitoringsystem.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient elasticsearchClient;
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexInitializer.class);

    @PostConstruct
    public void initIndexes() {
        createLogEventsIndex();
        createAuditIndex();
    }

    private void createLogEventsIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index("log-events-index")))
                    .value();

            if (!exists) {
                CreateIndexResponse response = elasticsearchClient.indices()
                        .create(c -> c
                                .index("log-events-index")
                                .mappings(m -> m
                                        .properties("@timestamp", p -> p.date(d -> d))
                                        .properties("createdAt",   p -> p.date(d -> d))
                                        .properties("serviceName", p -> p.keyword(k -> k))
                                        .properties("level",       p -> p.keyword(k -> k))
                                        .properties("message",     p -> p.text(t -> t))
                                        .properties("endpoint",    p -> p.keyword(k -> k))
                                        .properties("method",      p -> p.keyword(k -> k))
                                        .properties("username",    p -> p.keyword(k -> k))
                                )
                        );
                log.info("INDEX_CREATED index=log-events-index acknowledged={}",
                        response.acknowledged());
            } else {
                log.info("INDEX_ALREADY_EXISTS index=log-events-index");
            }

        } catch (Exception e) {
            log.error("INDEX_CREATION_FAILED index=log-events-index reason={}", e.getMessage());
        }
    }

    private void createAuditIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index("audit-index")))
                    .value();

            if (!exists) {
                CreateIndexResponse response = elasticsearchClient.indices()
                        .create(c -> c
                                .index("audit-index")
                                .mappings(m -> m
                                        .properties("@timestamp", p -> p.date(d -> d))
                                        .properties("username",   p -> p.keyword(k -> k))
                                        .properties("email",      p -> p.keyword(k -> k))
                                        .properties("endpoint",   p -> p.keyword(k -> k))
                                        .properties("method",     p -> p.keyword(k -> k))
                                )
                        );
                log.info("INDEX_CREATED index=audit-index acknowledged={}",
                        response.acknowledged());
            } else {
                log.info("INDEX_ALREADY_EXISTS index=audit-index");
            }

        } catch (Exception e) {
            log.error("INDEX_CREATION_FAILED index=audit-index reason={}", e.getMessage());
        }
    }
}
