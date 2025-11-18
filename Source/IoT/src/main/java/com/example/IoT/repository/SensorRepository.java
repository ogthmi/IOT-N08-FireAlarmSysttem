package com.example.IoT.repository;

import com.example.IoT.entity.SensorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<SensorEntity, Long> {
    List<SensorEntity> findAllByDeviceId(String deviceId);
    List<SensorEntity> findAllByDeviceIdIn(List<String> deviceIds);
}
