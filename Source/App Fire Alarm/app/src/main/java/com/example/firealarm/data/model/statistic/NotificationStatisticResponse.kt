package com.example.firealarm.data.model.statistic

import com.example.firealarm.domain.model.NotificationStatistic

data class NotificationStatisticResponse(
    val code: Int,
    val message: String,
    val result: NotificationStatisticResult
)

data class NotificationStatisticResult(
    val fireNotifications: List<NotificationStatisticDto>,
    val smokeNotifications: List<NotificationStatisticDto>
)

data class NotificationStatisticDto(
    val value: Double,
    val time: String
)

fun NotificationStatisticDto.toDomain(): NotificationStatistic {
    return NotificationStatistic(
        value = value,
        time = time
    )
}

