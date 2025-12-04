package com.example.firealarm.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.usecase.LoginUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {
    
    private val _loginState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val loginState: StateFlow<NetworkState> get() = _loginState
    
    fun login(username: String, password: String) {
        _loginState.value = NetworkState.Loading
        viewModelScope.launch {
            loginUseCase.execute(username, password).collect {
                _loginState.value = it
            }
        }
    }

}