package com.example.IoT.service;

import com.example.IoT.dto.request.threshold.ThresholdDTO;
import com.example.IoT.entity.DeviceEntity;
import com.example.IoT.entity.RuleEntity;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import com.example.IoT.repository.DeviceRepository;
import com.example.IoT.repository.RuleRepository;
import com.example.IoT.security.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RuleService {
    private final RuleRepository ruleRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public void changeThreshold(String deviceId, List<ThresholdDTO> thresholdDTOS, String accessToken) {
        String role = TokenHelper.getRoleFromToken(accessToken);
        if (role.equals("USER")) {
            Long userId = TokenHelper.getUserIdFromToken(accessToken);
            if (Boolean.FALSE.equals(deviceRepository.existsByUserIdAndDeviceId(userId, deviceId))) {
                throw new AppException(ErrorCode.DEVICE_NOT_FOUND);
            }

            if (Boolean.FALSE.equals(deviceRepository.existsByDeviceId(deviceId))) {
                throw new AppException(ErrorCode.DEVICE_NOT_FOUND);
            }
        }

        Map<String, RuleEntity> ruleEntityMap = ruleRepository.findAllByDeviceId(deviceId)
                .stream().collect(Collectors.toMap(RuleEntity::getSensorName, Function.identity()));
        for (ThresholdDTO thresholdDTO : thresholdDTOS) {
            RuleEntity ruleEntity = ruleEntityMap.get(thresholdDTO.getSensorName());
            ruleEntity.setThreshold(thresholdDTO.getThreshold());
            ruleRepository.save(ruleEntity);
        }
    }

    @Transactional(readOnly = true)
    public List<ThresholdDTO> getThresholds(String deviceId) {
        List<RuleEntity> ruleEntities = ruleRepository.findAllByDeviceId(deviceId);
        List<ThresholdDTO> thresholdDTOS = new ArrayList<>();
        for (RuleEntity ruleEntity : ruleEntities) {
            ThresholdDTO thresholdDTO = ThresholdDTO.builder()
                    .sensorName(ruleEntity.getSensorName())
                    .threshold(ruleEntity.getThreshold())
                    .ruleName(ruleEntity.getRuleName())
                    .build();
            thresholdDTOS.add(thresholdDTO);
        }
        return thresholdDTOS;
    }
}
