package com.example.IoT.repository;

import com.example.IoT.entity.TelemetryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryEntity, Long> {
    void deleteAllBySensorIdIn(List<Long> sensorIds);
    List<TelemetryEntity> findAllBySensorIdInAndCreatedAtAfter(
            List<Long> sensorIds,
            LocalDateTime time);

}
