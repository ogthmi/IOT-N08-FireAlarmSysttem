package com.example.IoT.dto.request.device;

import com.example.IoT.dto.request.SensorDTO;
import com.example.IoT.dto.request.threshold.ThresholdDTO;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class DeviceDTO {
    private String deviceId;
    private String deviceName;
    private String description;
    private Long userId;
    private List<SensorDTO> sensors;
    private List<ThresholdDTO> thresholds;
}
