package com.example.IoT.repository;

import com.example.IoT.entity.FirmwareVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirmwareVersionRepository extends JpaRepository<FirmwareVersionEntity, Long> {
    Optional<FirmwareVersionEntity> findByVersion(String version);
    Optional<FirmwareVersionEntity> findTopByOrderByVersionNumberDesc();
    boolean existsByVersion(String version);
}
