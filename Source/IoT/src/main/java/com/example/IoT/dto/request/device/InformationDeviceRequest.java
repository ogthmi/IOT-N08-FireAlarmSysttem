package com.example.IoT.dto.request.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InformationDeviceRequest {
    private String deviceName;
    private String description;
}
