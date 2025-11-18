package com.example.IoT.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_firmware_update")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirmwareUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private Long versionPreviousId;
    private Long versionNextId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
