package com.example.IoT.controller;

import com.example.IoT.constant.Constant;
import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.device.DeviceDTO;
import com.example.IoT.dto.request.device.InformationDeviceRequest;
import com.example.IoT.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Thay đổi thông tin thiết bị")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<?> updateDevice(@RequestBody InformationDeviceRequest informationDeviceRequest,
                                       @RequestParam String deviceId,
                                       @RequestHeader(Constant.AUTHORIZATION) String accessToken) {
        deviceService.changeInformationDevice(deviceId, informationDeviceRequest, accessToken);
        return ApiResponse.builder()
                .code(200)
                .message("Thay đổi thông tin thiết bị thành công")
                .build();
    }

    @Operation(summary = "Xóa thiết bị")
    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<?> deleteDevice(@RequestParam String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ApiResponse.builder()
                .code(200)
                .message("Xóa thông tin thiết bị thành công")
                .build();
    }
}
