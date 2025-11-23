package com.example.IoT.repository;

import com.example.IoT.entity.FirmwareUpdateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FirmwareUpdateRepository extends JpaRepository<FirmwareUpdateEntity, Long> {
    List<FirmwareUpdateEntity> findAllByDeviceId(String deviceId);
    Optional<FirmwareUpdateEntity> findByDeviceIdAndStatus(String deviceId, String status);
    Page<FirmwareUpdateEntity> findAllByDeviceId(String deviceId, Pageable pageable);
}
