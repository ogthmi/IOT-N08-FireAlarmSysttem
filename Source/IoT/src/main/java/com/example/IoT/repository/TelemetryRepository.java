package com.example.IoT.repository;

import com.example.IoT.entity.TelemetryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryEntity, Long> {
    void deleteAllBySensorIdIn(List<Long> sensorIds);
    @Query(value = "SELECT * FROM tbl_telemetry WHERE sensor_id IN :sensorIds ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<TelemetryEntity> findLatestForSensors(@Param("sensorIds") List<Long> sensorIds,
                                               @Param("limit") int limit);

}
