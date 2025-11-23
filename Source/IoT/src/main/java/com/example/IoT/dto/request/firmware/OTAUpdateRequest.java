package com.example.IoT.dto.request.firmware;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OTAUpdateRequest {
    private String deviceId;
    private String targetVersion;
}
