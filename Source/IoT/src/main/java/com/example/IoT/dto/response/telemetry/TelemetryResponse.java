package com.example.IoT.dto.response.telemetry;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class TelemetryResponse {
    private String deviceId;
    private String name;
    private String value;
    private String unit;
    private Boolean status;
}
