package com.example.IoT.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SensorDTO {
    private String name;
    private String unit;
}
