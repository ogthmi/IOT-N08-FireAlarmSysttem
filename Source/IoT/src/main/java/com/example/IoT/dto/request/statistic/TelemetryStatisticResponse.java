package com.example.IoT.dto.request.statistic;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class TelemetryStatisticResponse {
    private List<TemperatureDTO> temperatures;
    private List<SmokeDTO> smokes;
    private List<HumidityDTO> humidities;

}
