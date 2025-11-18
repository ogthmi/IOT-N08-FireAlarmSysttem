package com.example.IoT.dto.request.telemetry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TelemetryDevice {
    private String deviceId;
    private List<Telemetry> telemetries;
}
