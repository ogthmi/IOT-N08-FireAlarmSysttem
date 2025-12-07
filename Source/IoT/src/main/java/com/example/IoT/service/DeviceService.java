package com.example.IoT.service;

import com.example.IoT.dto.request.SensorDTO;
import com.example.IoT.dto.request.device.DeviceDTO;
import com.example.IoT.dto.request.device.InformationDeviceRequest;
import com.example.IoT.dto.request.threshold.ThresholdDTO;
import com.example.IoT.entity.DeviceEntity;
import com.example.IoT.entity.RuleEntity;
import com.example.IoT.entity.SensorEntity;
import com.example.IoT.entity.UserEntity;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import com.example.IoT.repository.*;
import com.example.IoT.security.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final UserRepository userRepository;
    private final RuleRepository ruleRepository;
    private final TelemetryRepository telemetryRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createDevice(DeviceDTO deviceDTO) {

        UserEntity userEntity = userRepository.findById(deviceDTO.getUserId()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        DeviceEntity deviceEntity = DeviceEntity.builder()
                .deviceId(deviceDTO.getDeviceId())
                .deviceName(deviceDTO.getDeviceName())
                .description(deviceDTO.getDescription())
                .userId(userEntity.getId())
                .build();
        deviceRepository.save(deviceEntity);

        for (SensorDTO sensorDTO : deviceDTO.getSensors()) {
            SensorEntity sensorEntity = SensorEntity.builder()
                    .sensorName(sensorDTO.getName())
                    .deviceId(deviceEntity.getDeviceId())
                    .unit(sensorDTO.getUnit() != null ? sensorDTO.getUnit() : null)
                    .build();
            sensorRepository.save(sensorEntity);
        }

        for (ThresholdDTO thresholdDTO : deviceDTO.getThresholds()) {
            RuleEntity ruleEntity = RuleEntity.builder()
                    .deviceId(deviceDTO.getDeviceId())
                    .threshold(thresholdDTO.getThreshold())
                    .sensorName(thresholdDTO.getSensorName())
                    .ruleName(thresholdDTO.getRuleName())
                    .build();
            ruleRepository.save(ruleEntity);
        }
    }

    @Transactional(readOnly = true)
    public Page<DeviceDTO> getDevices(Pageable pageable) {
        Page<DeviceEntity> deviceEntities = deviceRepository.findAll(pageable);
        return getDevicePage(deviceEntities);
    }

    @Transactional(readOnly = true)
    public Page<DeviceDTO> getDevicesByUserId(Pageable pageable, String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        Page<DeviceEntity> deviceEntities = deviceRepository.findAllByUserId(userId, pageable);
        if (deviceEntities.isEmpty() || deviceEntities == null) {
            return Page.empty();
        }
        return getDevicePage(deviceEntities);
    }

    @Transactional
    public void changeInformationDevice(String deviceId,
                                        InformationDeviceRequest informationDeviceRequest,
                                        String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        String role = TokenHelper.getRoleFromToken(accessToken);
        System.out.println(role);
        DeviceEntity deviceEntity = deviceRepository.findByDeviceId(deviceId);

        if (!role.equals("ADMIN") && userId != deviceEntity.getUserId()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        deviceEntity.setDeviceName(informationDeviceRequest.getDeviceName());
        deviceEntity.setDescription(informationDeviceRequest.getDescription());
        deviceRepository.save(deviceEntity);
    }

    @Transactional
    public void deleteDevice(String deviceId) {
        List<Long> sensorIds = sensorRepository.findAllByDeviceId(deviceId)
                .stream().map(SensorEntity::getId).collect(Collectors.toList());
        telemetryRepository.deleteAllBySensorIdIn(sensorIds);
        ruleRepository.deleteAllByDeviceIdIn(Arrays.asList(deviceId));
        notificationRepository.deleteAllByDeviceIdIn(Arrays.asList(deviceId));
        sensorRepository.deleteAllByDeviceIdIn(Arrays.asList(deviceId));
        deviceRepository.deleteByDeviceId(deviceId);
    }

    private Page<DeviceDTO> getDevicePage(Page<DeviceEntity> deviceEntities) {
        List<String> deviceIds = deviceEntities.stream().map(DeviceEntity::getDeviceId).collect(Collectors.toList());
        Map<Long, UserEntity> userEntities = userRepository.findAllByIdIn(
                deviceEntities.stream().map(DeviceEntity::getUserId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(UserEntity::getId, userEntity -> userEntity));
        Map<String, UserEntity> userEntityMap = new HashMap<>();
        for (DeviceEntity deviceEntity : deviceEntities) {
            UserEntity userEntity = userEntities.get(deviceEntity.getUserId());
            userEntityMap.put(deviceEntity.getDeviceId(), userEntity);
        }
        Map<String, List<SensorEntity>> sensorMap =
                sensorRepository.findAllByDeviceIdIn(deviceIds)
                        .stream()
                        .collect(Collectors.groupingBy(SensorEntity::getDeviceId));
        Map<String, List<RuleEntity>> ruleMap =
                ruleRepository.findAllByDeviceIdIn(deviceIds)
                        .stream()
                        .collect(Collectors.groupingBy(RuleEntity::getDeviceId));
        return deviceEntities.map(
                deviceEntity -> {
                    DeviceDTO deviceDTO = new DeviceDTO();
                    UserEntity userEntity = userEntityMap.get(deviceEntity.getDeviceId());
                    deviceDTO.setDeviceId(deviceEntity.getDeviceId());
                    deviceDTO.setDeviceName(deviceEntity.getDeviceName());
                    deviceDTO.setDescription(deviceEntity.getDescription());
                    deviceDTO.setUserId(userEntity.getId());
                    List<SensorDTO> sensorDTOS = new ArrayList<>();
                    List<ThresholdDTO> thresholdDTOS = new ArrayList<>();
                    for (SensorEntity sensorEntity : sensorMap.get(deviceEntity.getDeviceId())) {
                        SensorDTO sensorDTO = SensorDTO.builder()
                                .name(sensorEntity.getSensorName())
                                .unit(sensorEntity.getUnit())
                                .build();
                        sensorDTOS.add(sensorDTO);
                    }
                    deviceDTO.setSensors(sensorDTOS);
                    for (RuleEntity ruleEntity : ruleMap.get(deviceEntity.getDeviceId())) {
                        ThresholdDTO thresholdDTO = ThresholdDTO.builder()
                                .ruleName(ruleEntity.getRuleName())
                                .sensorName(ruleEntity.getSensorName())
                                .threshold(ruleEntity.getThreshold())
                                .build();
                        thresholdDTOS.add(thresholdDTO);
                    }
                    deviceDTO.setThresholds(thresholdDTOS);
                    return deviceDTO;
                }
        );
    }
}
