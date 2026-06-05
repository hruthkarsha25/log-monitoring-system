package com.project.logmonitoringsystem.repository;

import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.model.LogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEvent, Long>, JpaSpecificationExecutor<LogEvent> {

    List<LogEvent> findByLevel(LogLevel level);

    List<LogEvent> findByServiceName(String serviceName);

    List<LogEvent> findByLevelAndServiceName(LogLevel level, String serviceName);

    List<LogEvent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT l.level, count(l) FROM LogEvent l GROUP BY l.level")
    List<Object[]> countLogsByLevel();

    long countByCreatedAtAfter(LocalDateTime time);

    @Query("""
       SELECT l.serviceName, COUNT(l)
       FROM LogEvent l
       GROUP BY l.serviceName
       """)
    List<Object[]> countLogsByService();


    List<LogEvent> findByEndpoint(String path);
    
    List<LogEvent> findByUsername(String username);
}
