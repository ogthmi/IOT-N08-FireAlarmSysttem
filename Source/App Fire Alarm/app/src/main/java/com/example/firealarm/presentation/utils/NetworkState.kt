package com.example.firealarm.presentation.utils

sealed class NetworkState {
    data object Init: NetworkState()
    data object Loading: NetworkState()
    data class Success<T>(val data: T? = null): NetworkState()
    data class Error(val message: String): NetworkState()
}