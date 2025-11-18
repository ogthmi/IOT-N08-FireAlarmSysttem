package com.example.IoT.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_sensor")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private String sensorName;
    private String unit;
}
