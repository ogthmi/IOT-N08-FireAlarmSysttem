package com.example.IoT.controller;

import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.response.notification.NotificationDTO;
import com.example.IoT.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationDTO>> getNotifications(@RequestHeader("Authorization") String accessToken,
                                                               @RequestParam String deviceId,
                                                               @ParameterObject Pageable pageable) {
        return ApiResponse.<Page<NotificationDTO>>builder()
                .code(200)
                .message("Lấy thông báo thành công")
                .result(notificationService.getNotifications(accessToken, deviceId, pageable))
                .build();
    }
}
