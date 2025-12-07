package com.example.IoT.controller;

import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.statistic.NotificationStatisticResponse;
import com.example.IoT.dto.request.statistic.TelemetryStatisticResponse;
import com.example.IoT.service.StatisticService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/statistic")
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/telemetry")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<TelemetryStatisticResponse> telemetryStatistic(@RequestParam String deviceId) {
        return ApiResponse.<TelemetryStatisticResponse>builder()
                .code(200)
                .message("Lấy thống kê cảm biến thành công")
                .result(statisticService.statisticTelemetry(deviceId))
                .build();
    }

    @GetMapping("/notification")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<NotificationStatisticResponse> notificationStatistic(@RequestParam String deviceId) {
        return ApiResponse.<NotificationStatisticResponse>builder()
                .code(200)
                .message("Lấy thống kê thông báo thành công")
                .result(statisticService.statisticNotification(deviceId))
                .build();
    }
}
