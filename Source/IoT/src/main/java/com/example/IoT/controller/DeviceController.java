package com.example.IoT.controller;

import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.device.DeviceDTO;
import com.example.IoT.service.DeviceService;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<DeviceDTO>> getAllDevices(@ParameterObject Pageable pageable) {
        return ApiResponse.<Page<DeviceDTO>>builder()
                .code(200)
                .message("Lấy tất cả các thiết bị thành công")
                .result(deviceService.getDevices(pageable))
                .build();
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> createDevice(@RequestBody DeviceDTO deviceDTO) {
        deviceService.createDevice(deviceDTO);

        return ApiResponse.<DeviceDTO>builder()
                .code(200)
                .message("Thêm mới thiết bị thành công")
                .build();
    }

    @GetMapping("/user")
    public ApiResponse<Page<DeviceDTO>> getDevicesByUserId(@ParameterObject Pageable pageable,
                                                           @RequestHeader("Authorization") String accessToken) {
        return ApiResponse.<Page<DeviceDTO>>builder()
                .code(200)
                .message("Lấy các thiết bị thành công")
                .result(deviceService.getDevicesByUserId(pageable, accessToken))
                .build();
    }
}
