package com.example.IoT.dto.request.statistic;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SmokeDTO {
    private String value;
    private LocalDateTime createdAt;
}
