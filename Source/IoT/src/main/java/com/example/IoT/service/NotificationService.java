package com.example.IoT.service;

import com.example.IoT.dto.response.notification.NotificationDTO;
import com.example.IoT.entity.DeviceEntity;
import com.example.IoT.entity.NotificationEntity;
import com.example.IoT.mapper.NotificationMapper;
import com.example.IoT.repository.DeviceRepository;
import com.example.IoT.repository.NotificationRepository;
import com.example.IoT.security.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(String accessToken, String deviceId, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        DeviceEntity deviceEntity = deviceRepository.findByUserIdAndDeviceId(userId, deviceId);
        Page<NotificationEntity> notificationEntities = notificationRepository.findAllByDeviceId(deviceEntity.getDeviceId(), pageable);
        return notificationEntities.map(
                notificationEntity -> notificationMapper.getNotificationDTOFromEntity(notificationEntity)
        );
    }
}
