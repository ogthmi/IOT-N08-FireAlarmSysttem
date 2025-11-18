package com.example.IoT.controller;

import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.threshold.ThresholdDTO;
import com.example.IoT.service.RuleService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/rules")
public class RuleController {
    private final RuleService ruleService;

    @PostMapping("/threshold")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<?> changeThreshold(@RequestBody List<ThresholdDTO> thresholds,
                                          @RequestParam String deviceId,
                                          @RequestHeader("Authorization") String accessToken) {
        ruleService.changeThreshold(deviceId, thresholds, accessToken);

        return ApiResponse.builder()
                .code(200)
                .message("Thay đổi ngưỡng thành công")
                .build();
    }

    @GetMapping("/threshold")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<?> getThreshold(@RequestParam String deviceId) {
        return ApiResponse.builder()
                .code(200)
                .message("Xem ngưỡng thành công")
                .result(ruleService.getThresholds(deviceId))
                .build();
    }
}
