package com.example.firealarm.presentation.ui.user.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.usecase.GetSensorStatisticsUseCase
import com.example.firealarm.domain.usecase.GetNotificationStatisticsUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val getSensorStatisticsUseCase: GetSensorStatisticsUseCase,
    private val getNotificationStatisticsUseCase: GetNotificationStatisticsUseCase
) : ViewModel() {

    private val _sensorStatisticState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val sensorStatisticState: StateFlow<NetworkState> = _sensorStatisticState.asStateFlow()

    private val _notificationStatisticState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val notificationStatisticState: StateFlow<NetworkState> = _notificationStatisticState.asStateFlow()

    fun loadSensorStatistics(deviceId: String) {
        viewModelScope.launch {
            _sensorStatisticState.value = NetworkState.Loading
            getSensorStatisticsUseCase.execute(deviceId).collect { state ->
                _sensorStatisticState.value = state
            }
        }
    }

    fun loadNotificationStatistics(deviceId: String) {
        viewModelScope.launch {
            _notificationStatisticState.value = NetworkState.Loading
            getNotificationStatisticsUseCase.execute(deviceId).collect { state ->
                _notificationStatisticState.value = state
            }
        }
    }
}