package com.example.IoT.repository;

import com.example.IoT.entity.RuleEntity;
import org.apache.commons.codec.language.bm.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {
    List<RuleEntity> findAllByDeviceId(String deviceId);
    List<RuleEntity> findAllByDeviceIdIn(List<String> deviceIds);
    void deleteAllByDeviceIdIn(List<String> deviceIds);
}
