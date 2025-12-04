package com.example.IoT.service;

import com.example.IoT.config.MqttGateway;
import com.example.IoT.dto.request.firmware.FirmwareVersionDTO;
import com.example.IoT.dto.request.firmware.OTAUpdateRequest;
import com.example.IoT.dto.response.firmware.FirmwareUpdateResponse;
import com.example.IoT.entity.DeviceEntity;
import com.example.IoT.entity.FirmwareUpdateEntity;
import com.example.IoT.entity.FirmwareVersionEntity;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import com.example.IoT.repository.DeviceRepository;
import com.example.IoT.repository.FirmwareUpdateRepository;
import com.example.IoT.repository.FirmwareVersionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FirmwareService {
    private final FirmwareVersionRepository firmwareVersionRepository;
    private final FirmwareUpdateRepository firmwareUpdateRepository;
    private final DeviceRepository deviceRepository;
    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Tạo phiên bản firmware mới
     */
    @Transactional
    public FirmwareVersionDTO createFirmwareVersion(FirmwareVersionDTO dto) {
        if (firmwareVersionRepository.existsByVersion(dto.getVersion())) {
            throw new AppException(ErrorCode.FIRMWARE_VERSION_EXISTED);
        }

        // Validate downloadUrl is provided
        if (dto.getDownloadUrl() == null || dto.getDownloadUrl().isEmpty()) {
            throw new AppException(ErrorCode.FIRMWARE_URL_REQUIRED);
        }

        FirmwareVersionEntity entity = FirmwareVersionEntity.builder()
                .version(dto.getVersion())
                .versionNumber(dto.getVersionNumber())
                .firmwareUrl(dto.getDownloadUrl())
                .description(dto.getDescription())
                .releasedAt(LocalDateTime.now())
                .build();

        FirmwareVersionEntity saved = firmwareVersionRepository.save(entity);
        return convertToDTO(saved);
    }

    /**
     * Lấy tất cả phiên bản firmware
     */
    @Transactional(readOnly = true)
    public List<FirmwareVersionDTO> getAllFirmwareVersions() {
        return firmwareVersionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin firmware theo ID
     */
    @Transactional(readOnly = true)
    public FirmwareVersionDTO getFirmwareVersionById(Long id) {
        FirmwareVersionEntity entity = firmwareVersionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FIRMWARE_VERSION_NOT_FOUND));
        return convertToDTO(entity);
    }

    /**
     * Cập nhật thông tin firmware
     */
    @Transactional
    public FirmwareVersionDTO updateFirmwareVersion(Long id, FirmwareVersionDTO dto) {
        FirmwareVersionEntity entity = firmwareVersionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FIRMWARE_VERSION_NOT_FOUND));

        // Kiểm tra version mới đã tồn tại chưa (nếu thay đổi version)
        if (!entity.getVersion().equals(dto.getVersion()) && 
            firmwareVersionRepository.existsByVersion(dto.getVersion())) {
            throw new AppException(ErrorCode.FIRMWARE_VERSION_EXISTED);
        }

        // Cập nhật thông tin
        if (dto.getVersion() != null) {
            entity.setVersion(dto.getVersion());
        }
        if (dto.getVersionNumber() > 0) {
            entity.setVersionNumber(dto.getVersionNumber());
        }
        if (dto.getDownloadUrl() != null) {
            entity.setFirmwareUrl(dto.getDownloadUrl());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        FirmwareVersionEntity updated = firmwareVersionRepository.save(entity);
        return convertToDTO(updated);
    }

    /**
     * Xóa firmware version
     */
    @Transactional
    public void deleteFirmwareVersion(Long id) {
        FirmwareVersionEntity entity = firmwareVersionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FIRMWARE_VERSION_NOT_FOUND));

        // Kiểm tra xem có update nào đang sử dụng version này không
        List<FirmwareUpdateEntity> activeUpdates = firmwareUpdateRepository
                .findByVersionNextIdAndStatusIn(id, List.of("PENDING", "IN_PROGRESS"));
        
        if (!activeUpdates.isEmpty()) {
            throw new AppException(ErrorCode.FIRMWARE_UPDATE_IN_PROGRESS);
        }

        firmwareVersionRepository.delete(entity);
    }

    /**
     * Lấy phiên bản firmware mới nhất
     */
    @Transactional(readOnly = true)
    public FirmwareVersionDTO getLatestFirmwareVersion() {
        FirmwareVersionEntity latest = firmwareVersionRepository
                .findTopByOrderByVersionNumberDesc()
                .orElseThrow(() -> new AppException(ErrorCode.UNIT_NOT_FOUND));
        return convertToDTO(latest);
    }

    /**
     * Bắt đầu OTA update cho thiết bị
     */
    @Transactional
    public FirmwareUpdateResponse initiateOTAUpdate(OTAUpdateRequest request) throws JsonProcessingException {
        // Kiểm tra thiết bị tồn tại
        DeviceEntity device = deviceRepository.findByDeviceId(request.getDeviceId());
        if (device == null) {
            throw new AppException(ErrorCode.UNIT_NOT_FOUND);
        }

        // Kiểm tra phiên bản firmware target
        FirmwareVersionEntity targetVersion = firmwareVersionRepository
                .findByVersion(request.getTargetVersion())
                .orElseThrow(() -> new AppException(ErrorCode.FIRMWARE_VERSION_NOT_FOUND));

        // Kiểm tra xem có update đang chạy không
        firmwareUpdateRepository.findByDeviceIdAndStatus(request.getDeviceId(), "IN_PROGRESS")
                .ifPresent(update -> {
                    throw new AppException(ErrorCode.FIRMWARE_UPDATE_IN_PROGRESS);
                });

        // Tạo bản ghi firmware update
        FirmwareUpdateEntity updateEntity = FirmwareUpdateEntity.builder()
                .deviceId(request.getDeviceId())
                .versionPreviousId(null)
                .versionNextId(targetVersion.getId())
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .build();

        FirmwareUpdateEntity saved = firmwareUpdateRepository.save(updateEntity);

        // Gửi lệnh OTA qua MQTT
        sendOTACommand(request.getDeviceId(), targetVersion);

        // Cập nhật status sang IN_PROGRESS
        saved.setStatus("IN_PROGRESS");
        firmwareUpdateRepository.save(saved);

        return convertToUpdateResponse(saved, null, targetVersion);
    }

    /**
     * Hủy OTA update đang thực hiện
     */
    @Transactional
    public FirmwareUpdateResponse cancelOTAUpdate(String deviceId) throws JsonProcessingException {
        // Tìm update đang IN_PROGRESS
        FirmwareUpdateEntity updateEntity = firmwareUpdateRepository
                .findByDeviceIdAndStatus(deviceId, "IN_PROGRESS")
                .orElseThrow(() -> new AppException(ErrorCode.FIRMWARE_UPDATE_NOT_FOUND));

        // Gửi lệnh CANCEL qua MQTT
        sendCancelCommand(deviceId);

        // Cập nhật status thành CANCELLED
        updateEntity.setStatus("CANCELLED");
        updateEntity.setEndTime(LocalDateTime.now());
        FirmwareUpdateEntity saved = firmwareUpdateRepository.save(updateEntity);

        FirmwareVersionEntity next = firmwareVersionRepository
                .findById(saved.getVersionNextId())
                .orElse(null);

        return convertToUpdateResponse(saved, null, next);
    }

    /**
     * Gửi lệnh OTA update qua MQTT
     */
    private void sendOTACommand(String deviceId, FirmwareVersionEntity firmware) throws JsonProcessingException {
        Map<String, Object> otaCommand = new HashMap<>();
        otaCommand.put("command", "OTA_UPDATE");
        otaCommand.put("version", firmware.getVersion());
        otaCommand.put("versionNumber", firmware.getVersionNumber());
        otaCommand.put("downloadUrl", generateFirmwareUrl(firmware));
        otaCommand.put("timestamp", LocalDateTime.now().format(formatter));

        String jsonPayload = objectMapper.writeValueAsString(otaCommand);
        String topic = "iot/device/" + deviceId + "/ota";

        mqttGateway.sendToTopic(topic, jsonPayload);
        System.out.println("OTA command sent to device: " + deviceId + " - Topic: " + topic);
    }

    /**
     * Gửi lệnh CANCEL OTA qua MQTT
     */
    private void sendCancelCommand(String deviceId) throws JsonProcessingException {
        Map<String, Object> cancelCommand = new HashMap<>();
        cancelCommand.put("command", "OTA_CANCEL");
        cancelCommand.put("timestamp", LocalDateTime.now().format(formatter));

        String jsonPayload = objectMapper.writeValueAsString(cancelCommand);
        String topic = "iot/device/" + deviceId + "/ota";

        mqttGateway.sendToTopic(topic, jsonPayload);
        System.out.println("OTA cancel command sent to device: " + deviceId + " - Topic: " + topic);
    }

    /**
     * Lấy lịch sử update của thiết bị
     */
    @Transactional(readOnly = true)
    public Page<FirmwareUpdateResponse> getDeviceUpdateHistory(String deviceId, Pageable pageable) {
        Page<FirmwareUpdateEntity> updates = firmwareUpdateRepository
                .findAllByDeviceId(deviceId, pageable);

        return updates.map(update -> {
            FirmwareVersionEntity previous = update.getVersionPreviousId() != null
                    ? firmwareVersionRepository.findById(update.getVersionPreviousId()).orElse(null)
                    : null;
            FirmwareVersionEntity next = firmwareVersionRepository
                    .findById(update.getVersionNextId())
                    .orElse(null);

            return convertToUpdateResponse(update, previous, next);
        });
    }

    /**
     * Kiểm tra cập nhật cho thiết bị
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkForUpdate(String deviceId, String currentVersion) {
        FirmwareVersionEntity current = firmwareVersionRepository
                .findByVersion(currentVersion)
                .orElse(null);
        
        FirmwareVersionEntity latest = firmwareVersionRepository
                .findTopByOrderByVersionNumberDesc()
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        
        if (current != null && latest != null && latest.getVersionNumber() > current.getVersionNumber()) {
            result.put("updateAvailable", true);
            result.put("latestVersion", convertToDTO(latest));
            result.put("currentVersion", convertToDTO(current));
        } else {
            result.put("updateAvailable", false);
            result.put("message", "Device is up to date");
        }

        return result;
    }

    /**
     * Generate firmware download URL
     */
    private String generateFirmwareUrl(FirmwareVersionEntity firmware) {
        return firmware.getFirmwareUrl() != null ? firmware.getFirmwareUrl() : null;
    }

    // Helper methods
    private FirmwareVersionDTO convertToDTO(FirmwareVersionEntity entity) {
        return FirmwareVersionDTO.builder()
                .id(entity.getId())
                .version(entity.getVersion())
                .versionNumber(entity.getVersionNumber())
                .releasedAt(entity.getReleasedAt().format(formatter))
                .downloadUrl(generateFirmwareUrl(entity))
                .description(entity.getDescription())
                .build();
    }

    private FirmwareUpdateResponse convertToUpdateResponse(
            FirmwareUpdateEntity update,
            FirmwareVersionEntity previous,
            FirmwareVersionEntity next) {
        return FirmwareUpdateResponse.builder()
                .id(update.getId())
                .deviceId(update.getDeviceId())
                .previousVersion(previous != null ? previous.getVersion() : "Unknown")
                .nextVersion(next != null ? next.getVersion() : "Unknown")
                .status(update.getStatus())
                .startTime(update.getStartTime().format(formatter))
                .endTime(update.getEndTime() != null ? update.getEndTime().format(formatter) : null)
                .build();
    }
}
