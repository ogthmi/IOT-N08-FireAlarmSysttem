package com.example.IoT.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_firmware")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirmwareVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String version;
    private int versionNumber;
    private LocalDateTime releasedAt;
    
    @Column(length = 500)
    private String firmwareUrl;
    
    @Column(length = 1000)
    private String description; 
}
