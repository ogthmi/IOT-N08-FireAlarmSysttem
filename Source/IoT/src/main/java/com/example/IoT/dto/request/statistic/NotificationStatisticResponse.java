package com.example.IoT.dto.request.statistic;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class NotificationStatisticResponse {
    private List<FireNotification> fireNotifications;
    private List<SmokeNotification> smokeNotifications;
}
