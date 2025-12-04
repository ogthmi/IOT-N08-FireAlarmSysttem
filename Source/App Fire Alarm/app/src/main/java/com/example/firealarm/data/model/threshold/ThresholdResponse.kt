package com.example.firealarm.data.model.threshold

import com.example.firealarm.domain.model.Threshold

data class ThresholdResponse(
    val code: Int,
    val message: String,
    val result: List<ThresholdDto>?
)

data class ThresholdDto(
    val sensorName: String,
    val ruleName: String,
    val threshold: Double
)

fun ThresholdDto.toDomain(): Threshold {
    return Threshold(
        sensorName = sensorName,
        ruleName = ruleName,
        threshold = threshold
    )
}

