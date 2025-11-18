package com.example.IoT.dto.request.threshold;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ThresholdDTO {
    private String sensorName;
    private String ruleName;
    private Double threshold;
}
