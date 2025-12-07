package com.example.IoT.repository;

import com.example.IoT.entity.DeviceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    boolean existsByDeviceId(String deviceId);
    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);
    DeviceEntity findByDeviceId(String deviceId);
    DeviceEntity findByUserIdAndDeviceId(Long userId, String deviceId);
    Page<DeviceEntity> findAllByUserId(Long userId, Pageable pageable);
    List<DeviceEntity> findAllByUserId(Long userId);
    void deleteAllByUserId(Long userId);
    void deleteByDeviceId(String deviceId);
}
