package com.project.logmonitoringsystem.alert;

import com.project.logmonitoringsystem.alert.entity.Alert;
import com.project.logmonitoringsystem.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus status);

    Optional<Alert> findByTypeAndEndpointAndStatus(
            String type,
            String endpoint,
            AlertStatus status
    );
}
