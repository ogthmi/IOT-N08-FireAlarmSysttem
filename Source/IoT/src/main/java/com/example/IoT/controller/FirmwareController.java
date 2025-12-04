package com.example.IoT.controller;

import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.firmware.FirmwareVersionDTO;
import com.example.IoT.dto.request.firmware.OTAUpdateRequest;
import com.example.IoT.dto.response.firmware.FirmwareUpdateResponse;
import com.example.IoT.service.FirmwareService;
import com.example.IoT.service.FirmwareStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/firmware")
@AllArgsConstructor
@CrossOrigin("*")
public class FirmwareController {

    private final FirmwareService firmwareService;
    private final FirmwareStorageService firmwareStorageService;

    /**
     * Tạo phiên bản firmware mới
     * POST /api/v1/firmware/versions
     */
    @PostMapping("/versions")
    public ApiResponse<FirmwareVersionDTO> createFirmwareVersion(
            @RequestBody FirmwareVersionDTO firmwareVersionDTO) {
        FirmwareVersionDTO created = firmwareService.createFirmwareVersion(firmwareVersionDTO);
        return ApiResponse.<FirmwareVersionDTO>builder()
                .code(200)
                .message("Firmware version created successfully")
                .result(created)
                .build();
    }

    /**
     * Lấy tất cả phiên bản firmware
     * GET /api/v1/firmware/versions
     */
    @GetMapping("/versions")
    public ApiResponse<List<FirmwareVersionDTO>> getAllFirmwareVersions() {
        List<FirmwareVersionDTO> versions = firmwareService.getAllFirmwareVersions();
        return ApiResponse.<List<FirmwareVersionDTO>>builder()
                .code(200)
                .message("Get all firmware versions successfully")
                .result(versions)
                .build();
    }

    /**
     * Lấy phiên bản firmware mới nhất
     * GET /api/v1/firmware/versions/latest
     */
    @GetMapping("/versions/latest")
    public ApiResponse<FirmwareVersionDTO> getLatestFirmwareVersion() {
        FirmwareVersionDTO latest = firmwareService.getLatestFirmwareVersion();
        return ApiResponse.<FirmwareVersionDTO>builder()
                .code(200)
                .message("Get latest firmware version successfully")
                .result(latest)
                .build();
    }

    /**
     * Lấy thông tin firmware theo ID
     * GET /api/v1/firmware/versions/{id}
     */
    @GetMapping("/versions/{id}")
    public ApiResponse<FirmwareVersionDTO> getFirmwareVersionById(@PathVariable Long id) {
        FirmwareVersionDTO firmware = firmwareService.getFirmwareVersionById(id);
        return ApiResponse.<FirmwareVersionDTO>builder()
                .code(200)
                .message("Get firmware version successfully")
                .result(firmware)
                .build();
    }

    /**
     * Cập nhật thông tin firmware
     * PUT /api/v1/firmware/versions/{id}
     */
    @PostMapping("/versions/{id}")
    public ApiResponse<FirmwareVersionDTO> updateFirmwareVersion(
            @PathVariable Long id,
            @RequestBody FirmwareVersionDTO firmwareVersionDTO) {
        FirmwareVersionDTO updated = firmwareService.updateFirmwareVersion(id, firmwareVersionDTO);
        return ApiResponse.<FirmwareVersionDTO>builder()
                .code(200)
                .message("Firmware version updated successfully")
                .result(updated)
                .build();
    }

    /**
     * Xóa firmware version
     * DELETE /api/v1/firmware/versions/{id}
     */
    @DeleteMapping("/versions/{id}")
    public ApiResponse<String> deleteFirmwareVersion(@PathVariable Long id) {
        firmwareService.deleteFirmwareVersion(id);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Firmware version deleted successfully")
                .result("Firmware ID: " + id)
                .build();
    }

    /**
     * Bắt đầu OTA update cho thiết bị
     * POST /api/firmware/ota/start
     */
    @PostMapping("/ota/start")
    public ApiResponse<FirmwareUpdateResponse> startOTAUpdate(
            @RequestBody OTAUpdateRequest request) throws JsonProcessingException {
        FirmwareUpdateResponse response = firmwareService.initiateOTAUpdate(request);
        return ApiResponse.<FirmwareUpdateResponse>builder()
                .code(200)
                .message("OTA update initiated successfully")
                .result(response)
                .build();
    }

    /**
     * Hủy OTA update đang thực hiện
     * POST /api/v1/firmware/ota/cancel
     */
    @PostMapping("/ota/cancel")
    public ApiResponse<FirmwareUpdateResponse> cancelOTAUpdate(
            @RequestParam String deviceId) throws JsonProcessingException {
        FirmwareUpdateResponse response = firmwareService.cancelOTAUpdate(deviceId);
        return ApiResponse.<FirmwareUpdateResponse>builder()
                .code(200)
                .message("OTA update cancelled successfully")
                .result(response)
                .build();
    }

    /**
     * Cập nhật trạng thái OTA (được gọi từ thiết bị hoặc callback)
     * POST /api/v1/firmware/ota/status
     * Note: Method này được gọi tự động qua MQTT callback trong MqttService
     */
    @PostMapping("/ota/status")
    public ApiResponse<String> updateOTAStatus(
            @RequestParam String deviceId,
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "0") Integer progress) {

        return ApiResponse.<String>builder()
                .code(200)
                .message("Use MQTT to send status updates. This endpoint is for testing only.")
                .result("Status: " + status)
                .build();
    }

    /**
     * Lấy lịch sử update của thiết bị
     * GET /api/v1/firmware/history/{deviceId}
     */
    @GetMapping("/history/{deviceId}")
    public ApiResponse<Page<FirmwareUpdateResponse>> getUpdateHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<FirmwareUpdateResponse> history = firmwareService.getDeviceUpdateHistory(deviceId, pageable);
        return ApiResponse.<Page<FirmwareUpdateResponse>>builder()
                .code(200)
                .message("Get device update history successfully")
                .result(history)
                .build();
    }

    /**
     * Kiểm tra cập nhật có sẵn cho thiết bị
     * GET /api/v1/firmware/check-update
     */
    @GetMapping("/check-update")
    public ApiResponse<Map<String, Object>> checkForUpdate(
            @RequestParam String deviceId,
            @RequestParam String currentVersion) {
        Map<String, Object> result = firmwareService.checkForUpdate(deviceId, currentVersion);
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Check for update completed")
                .result(result)
                .build();
    }

    /**
     * Upload firmware file to Cloudinary
     * POST /api/v1/firmware/upload
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> uploadFirmware(
            @RequestParam("file") MultipartFile file,
            @RequestParam("version") String version) {
        // Upload to Cloudinary
        String cloudinaryUrl = firmwareStorageService.uploadFirmware(file, version);
        
        Map<String, String> result = Map.of(
                "version", version,
                "cloudinaryUrl", cloudinaryUrl,
                "filename", file.getOriginalFilename(),
                "size", String.valueOf(file.getSize())
        );
        
        return ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Firmware file uploaded to Cloudinary successfully")
                .result(result)
                .build();
    }

    /**
     * Upload firmware + Create version 
     * POST /api/v1/firmware/upload-and-create
     */
    @PostMapping("/upload-and-create")
    public ApiResponse<FirmwareVersionDTO> uploadAndCreateVersion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("version") String version,
            @RequestParam("versionNumber") int versionNumber,
            @RequestParam(value = "description", required = false) String description) {
        
        // 1. Upload to Cloudinary
        String cloudinaryUrl = firmwareStorageService.uploadFirmware(file, version);
        
        // 2. Create firmware version with URL
        FirmwareVersionDTO dto = FirmwareVersionDTO.builder()
                .version(version)
                .versionNumber(versionNumber)
                .downloadUrl(cloudinaryUrl)
                .description(description)
                .build();
        
        FirmwareVersionDTO created = firmwareService.createFirmwareVersion(dto);
        
        return ApiResponse.<FirmwareVersionDTO>builder()
                .code(200)
                .message("Firmware uploaded and version created successfully")
                .result(created)
                .build();
    }

}
