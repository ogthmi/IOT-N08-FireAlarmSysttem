package com.example.IoT.service;

import com.example.IoT.constant.Constant;
import com.example.IoT.dto.request.telemetry.Telemetry;
import com.example.IoT.dto.request.telemetry.TelemetryDevice;
import com.example.IoT.dto.response.notification.NotificationDTO;
import com.example.IoT.dto.response.telemetry.TelemetryResponse;
import com.example.IoT.entity.*;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import com.example.IoT.mapper.NotificationMapper;
import com.example.IoT.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MqttService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final TelemetryRepository telemetryRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final RuleRepository ruleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final FirmwareUpdateRepository firmwareUpdateRepository;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    @Transactional
    public void receiveMessage(Message<String> message) throws MessagingException, JsonProcessingException {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload();

        System.out.println("---------------------------------------------");
        System.out.println("Message Received!");
        System.out.println("Topic: " + topic);
        System.out.println("Payload: " + payload);
        System.out.println("---------------------------------------------");

        if (topic.equals("iot/status")) {
            handleStatusUpdate(payload);
            return;
        }

        if (topic.equals("iot/data")) {
            try {
                TelemetryDevice telemetryDevice = objectMapper.readValue(payload, TelemetryDevice.class);

                if (telemetryDevice != null) {
                    List<TelemetryResponse> telemetryResponses = new ArrayList<>();
                    if (Boolean.TRUE.equals(deviceRepository.existsByDeviceId(telemetryDevice.getDeviceId()))) {

                        UserEntity userEntity = userRepository.findById(
                                deviceRepository.findByDeviceId(telemetryDevice.getDeviceId()).getUserId()
                        ).orElseThrow(
                                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
                        );

                        CompletableFuture.runAsync(() -> {
                            overThreshold(telemetryDevice.getDeviceId(), telemetryDevice.getTelemetries(), userEntity.getId());
                        });

                        Map<String, SensorEntity> sensorEntityMap = sensorRepository.findAllByDeviceId(telemetryDevice.getDeviceId())
                                .stream().collect(Collectors.toMap(SensorEntity::getSensorName, Function.identity()));
                        for (Telemetry telemetry : telemetryDevice.getTelemetries()) {
                            SensorEntity sensorEntity = sensorEntityMap.get(telemetry.getName());
                            TelemetryEntity telemetryEntity = TelemetryEntity.builder()
                                    .sensorId(sensorEntity.getId())
                                    .value(telemetry.getValue() != null ? telemetry.getValue() : null)
                                    .status(telemetry.getStatus() != null ? telemetry.getStatus() : null)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            telemetryRepository.save(telemetryEntity);

                            telemetryResponses.add(
                                    TelemetryResponse.builder()
                                            .deviceId(telemetryDevice.getDeviceId())
                                            .name(telemetry.getName())
                                            .value(telemetry.getValue() != null ? telemetry.getValue() : null)
                                            .status(telemetry.getStatus() != null ? telemetry.getStatus() : null)
                                            .unit(sensorEntity.getUnit() != null ? sensorEntity.getUnit() : null)
                                            .build()
                            );
                        }
                        sendDataToUser(userEntity.getId(), telemetryResponses);
                    }
                }
            } catch (Exception e) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
    }

    private void sendDataToUser(Long userId, List<TelemetryResponse> telemetryResponse) {
        System.out.println("======================================SEND USER ==========================");
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/data",
                telemetryResponse
        );
    }

    private void overThreshold(String deviceId, List<Telemetry> telemetries, Long userId) {
        Map<String, RuleEntity> ruleEntityMap = ruleRepository.findAllByDeviceId(deviceId)
                .stream().collect(Collectors.toMap(RuleEntity::getSensorName, Function.identity()));
        for (Telemetry telemetry : telemetries) {
            if (ruleEntityMap.containsKey(telemetry.getName())) {
                RuleEntity ruleEntity = ruleEntityMap.get(telemetry.getName());
                if (ruleEntity.getThreshold() < Double.parseDouble(telemetry.getValue())) {
                    switch (ruleEntity.getRuleName()) {
                        case "fire_threshold":
                            NotificationEntity fireNotification = NotificationEntity.builder()
                                    .deviceId(deviceId)
                                    .title("Phát hiện vượt mức ngưỡng nhiệt độ, cảnh báo cháy")
                                    .type(Constant.TEMPERATURE)
                                    .value(Double.parseDouble(telemetry.getValue()))
                                    .timestamp(LocalDateTime.now())
                                    .build();

                            notificationRepository.save(fireNotification);
                            sendNotificationToUser(userId, notificationMapper.getNotificationDTOFromEntity(fireNotification));
                            break;
                        case "smoke_threshold":
                            NotificationEntity smokeNotification = NotificationEntity.builder()
                                    .deviceId(deviceId)
                                    .title("Phát hiện vượt mức ngưỡng khí/khói, cảnh báo cháy")
                                    .type(Constant.SMOKE)
                                    .value(Double.parseDouble(telemetry.getValue()))
                                    .timestamp(LocalDateTime.now())
                                    .build();

                            notificationRepository.save(smokeNotification);
                            sendNotificationToUser(userId, notificationMapper.getNotificationDTOFromEntity(smokeNotification));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void sendNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        System.out.println("======================================SEND NOTIFICATION==========================");
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/notification",
                notificationDTO
        );
    }

    /**
     * Handle status updates from devices (including OTA updates)
     */
    private void handleStatusUpdate(String payload) throws JsonProcessingException {
        @SuppressWarnings("unchecked")
        Map<String, Object> statusData = objectMapper.readValue(payload, Map.class);

        String deviceId = (String) statusData.get("deviceId");
        String status = (String) statusData.get("status");

        System.out.println("Status update from device: " + deviceId + " - Status: " + status);

        // Check if it's an OTA update status
        if (status != null && (status.equals("IN_PROGRESS") ||
                               status.equals("COMPLETED") ||
                               status.equals("FAILED") ||
                               status.equals("SKIPPED"))) {
            Integer progress = statusData.get("progress") != null
                ? (Integer) statusData.get("progress")
                : 0;

            updateOTAStatus(deviceId, status, progress);
        }
    }

    /**
     * Update OTA status directly (to avoid circular dependency)
     */
    @Transactional
    public void updateOTAStatus(String deviceId, String status, Integer progress) {
        FirmwareUpdateEntity update = firmwareUpdateRepository
                .findByDeviceIdAndStatus(deviceId, "IN_PROGRESS")
                .orElse(null);

        if (update != null) {
            update.setStatus(status);

            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                update.setEndTime(LocalDateTime.now());
            }

            firmwareUpdateRepository.save(update);

            // Send notification via WebSocket
            DeviceEntity device = deviceRepository.findByDeviceId(deviceId);
            if (device != null) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("deviceId", deviceId);
                notification.put("status", status);
                notification.put("progress", progress);
                notification.put("updateId", update.getId());

                messagingTemplate.convertAndSendToUser(
                        device.getUserId().toString(),
                        "/firmware",
                        notification
                );
            }
        }
    }
}