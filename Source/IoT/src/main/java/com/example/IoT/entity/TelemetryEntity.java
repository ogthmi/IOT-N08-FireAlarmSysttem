package com.example.IoT.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_telemetry")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TelemetryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sensorId;
    private String value;
    private Boolean status;
    private LocalDateTime createdAt;
}
