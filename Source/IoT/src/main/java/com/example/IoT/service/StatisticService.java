package com.example.IoT.service;

import com.example.IoT.constant.Constant;
import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.statistic.*;
import com.example.IoT.entity.NotificationEntity;
import com.example.IoT.entity.SensorEntity;
import com.example.IoT.entity.TelemetryEntity;
import com.example.IoT.repository.DeviceRepository;
import com.example.IoT.repository.NotificationRepository;
import com.example.IoT.repository.SensorRepository;
import com.example.IoT.repository.TelemetryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StatisticService {
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public TelemetryStatisticResponse statisticTelemetry(String deviceId) {
        List<String> allowedNames = Arrays.asList("MP2", "DHT22T", "DHT22H");

        Map<Long, SensorEntity> sensorEntityMap =
                sensorRepository.findAllByDeviceId(deviceId)
                        .stream()
                        .filter(s -> allowedNames.contains(s.getSensorName()))
                        .collect(Collectors.toMap(
                                SensorEntity::getId,
                                s -> s
                        ));

        List<Long> sensorIds = new ArrayList<>(sensorEntityMap.keySet());
        LocalDateTime fourMinutesAgo = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusMinutes(4);

        List<TelemetryEntity> telemetryEntities = telemetryRepository
                .findAllBySensorIdInAndCreatedAtAfter(sensorIds, fourMinutesAgo);

        List<TemperatureDTO> temperatureDTOS = new ArrayList<>();
        List<SmokeDTO> smokeDTOS = new ArrayList<>();
        List<HumidityDTO> humidityDTOS = new ArrayList<>();

        for (TelemetryEntity telemetryEntity : telemetryEntities) {
            SensorEntity sensorEntity = sensorEntityMap.get(telemetryEntity.getSensorId());
            if (sensorEntity.getSensorName().equals("DHT22T")) {
                TemperatureDTO temperatureDTO = TemperatureDTO.builder()
                        .value(telemetryEntity.getValue())
                        .createdAt(telemetryEntity.getCreatedAt())
                        .build();
                temperatureDTOS.add(temperatureDTO);
            } else if (sensorEntity.getSensorName().equals("MP2")) {
                SmokeDTO smokeDTO = SmokeDTO.builder()
                        .value(telemetryEntity.getValue())
                        .createdAt(telemetryEntity.getCreatedAt())
                        .build();
                smokeDTOS.add(smokeDTO);
            } else {
                HumidityDTO humidityDTO = HumidityDTO.builder()
                        .value(telemetryEntity.getValue())
                        .createdAt(telemetryEntity.getCreatedAt())
                        .build();
                humidityDTOS.add(humidityDTO);
            }
        }

        System.out.println(temperatureDTOS.size());
        System.out.println(humidityDTOS.size());
        System.out.println(smokeDTOS.size());

        return TelemetryStatisticResponse.builder()
                .temperatures(temperatureDTOS)
                .smokes(smokeDTOS)
                .humidities(humidityDTOS)
                .build();

    }

    @Transactional(readOnly = true)
    public NotificationStatisticResponse statisticNotification(String deviceId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<NotificationEntity> notificationEntities = notificationRepository
                .findAllByDeviceIdAndTimestampAfter(deviceId, oneWeekAgo);

        List<FireNotification> fireNotifications = new ArrayList<>();
        List<SmokeNotification> smokeNotifications = new ArrayList<>();

        for (NotificationEntity notificationEntity : notificationEntities) {
            if (notificationEntity.getType().equals(Constant.TEMPERATURE)) {
                FireNotification fireNotification = FireNotification.builder()
                        .value(notificationEntity.getValue())
                        .time(notificationEntity.getTimestamp())
                        .build();
                fireNotifications.add(fireNotification);
            } else {
                SmokeNotification smokeNotification = SmokeNotification.builder()
                        .value(notificationEntity.getValue())
                        .time(notificationEntity.getTimestamp())
                        .build();
                smokeNotifications.add(smokeNotification);
            }
        }
        return NotificationStatisticResponse.builder()
                .fireNotifications(fireNotifications)
                .smokeNotifications(smokeNotifications)
                .build();
    }
}
