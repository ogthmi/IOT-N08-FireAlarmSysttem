package com.example.IoT.dto.response.firmware;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirmwareUpdateResponse {
    private Long id;
    private String deviceId;
    private String previousVersion;
    private String nextVersion;
    private String status;
    private String startTime;
    private String endTime;
    private Integer progress;
}
