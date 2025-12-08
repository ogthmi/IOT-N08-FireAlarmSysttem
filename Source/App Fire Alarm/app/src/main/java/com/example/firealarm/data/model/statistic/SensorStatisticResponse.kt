package com.example.firealarm.data.model.statistic

import com.example.firealarm.domain.model.SensorStatistic

data class SensorStatisticResponse(
    val code: Int,
    val message: String,
    val result: SensorStatisticResult
)

data class SensorStatisticResult(
    val temperatures: List<SensorStatisticDto>,
    val smokes: List<SensorStatisticDto>,
    val humidities: List<SensorStatisticDto>
)

data class SensorStatisticDto(
    val value: String,
    val createdAt: String
)

fun SensorStatisticDto.toDomain(): SensorStatistic {
    return SensorStatistic(
        value = value,
        createdAt = createdAt
    )
}

