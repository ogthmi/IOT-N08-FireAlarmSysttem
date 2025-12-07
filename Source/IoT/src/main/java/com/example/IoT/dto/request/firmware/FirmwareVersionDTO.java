package com.example.IoT.dto.request.firmware;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirmwareVersionDTO {
    private Long id;
    private String version;
    private int versionNumber;
    private String releasedAt;
    private String downloadUrl;
    private String description;
}
