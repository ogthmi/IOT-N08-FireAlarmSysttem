package com.example.IoT.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_device")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private Long userId;
    private String deviceName;
    private String description;
}
