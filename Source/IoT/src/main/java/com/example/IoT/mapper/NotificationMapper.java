package com.example.IoT.mapper;

import com.example.IoT.dto.response.notification.NotificationDTO;
import com.example.IoT.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {
    NotificationDTO getNotificationDTOFromEntity(NotificationEntity notificationEntity);
}
