package com.example.IoT.dto.request.telemetry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Telemetry {
    private String name;
    private String value;
    private Boolean status;
}