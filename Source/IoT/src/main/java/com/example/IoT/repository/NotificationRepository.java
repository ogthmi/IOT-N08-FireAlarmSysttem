package com.example.IoT.repository;

import com.example.IoT.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Page<NotificationEntity> findAllByDeviceId(String deviceId, Pageable pageable);
    void deleteAllByDeviceIdIn(List<String> deviceIds);
    List<NotificationEntity> findAllByDeviceIdAndTimestampAfter(String deviceId, LocalDateTime timestamp);
}
