package com.example.IoT.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class FirmwareStorageService {

    private final Cloudinary cloudinary;

    public FirmwareStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Upload firmware file lên Cloudinary
     */
    public String uploadFirmware(MultipartFile file, String version) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new AppException(ErrorCode.FIRMWARE_FILE_EMPTY);
            }

            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".bin")) {
                throw new AppException(ErrorCode.FIRMWARE_FILE_INVALID);
            }

            // Upload to Cloudinary với public_id = firmware/{version}
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "iot-firmware",
                            "public_id", version + "_firmware",
                            "resource_type", "raw", // Important: raw for binary files
                            "overwrite", true
                    ));

            String firmwareUrl = (String) uploadResult.get("secure_url");
            log.info("Firmware uploaded to Cloudinary: {}", firmwareUrl);

            return firmwareUrl;

        } catch (IOException e) {
            log.error("Failed to upload firmware to Cloudinary", e);
            throw new RuntimeException("Failed to upload firmware file", e);
        }
    }

    /**
     * Xóa firmware file khỏi Cloudinary
     */
    public void deleteFirmware(String version) {
        try {
            String publicId = "iot-firmware/" + version + "_firmware";
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "raw"));
            log.info("Firmware deleted from Cloudinary: {}", result);
        } catch (Exception e) {
            log.error("Failed to delete firmware from Cloudinary for version: {}", version, e);
        }
    }
}
