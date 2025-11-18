package com.example.IoT.dto.response.notification;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class NotificationDTO {
    private String deviceId;
    private String title;
    private LocalDateTime timestamp;
}
