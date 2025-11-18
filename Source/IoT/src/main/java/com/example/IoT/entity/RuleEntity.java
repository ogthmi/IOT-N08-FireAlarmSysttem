package com.example.IoT.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_rule")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ruleName;
    private String sensorName;
    private Double threshold;
    private String deviceId;
}
