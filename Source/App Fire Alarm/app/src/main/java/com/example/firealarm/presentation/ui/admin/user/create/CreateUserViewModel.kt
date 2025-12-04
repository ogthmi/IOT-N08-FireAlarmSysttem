package com.example.firealarm.presentation.ui.admin.user.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.repository.UserRepository
import com.example.firealarm.domain.usecase.CreateUserUseCase
import com.example.firealarm.domain.usecase.GetUserInformationUseCase
import com.example.firealarm.domain.usecase.UpdateUserUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val getUserInformationUseCase: GetUserInformationUseCase
): ViewModel() {
    
    private val _createState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val createState: StateFlow<NetworkState> = _createState.asStateFlow()

    private val _updateState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val updateState: StateFlow<NetworkState> = _updateState.asStateFlow()

    private val _inforState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val inforState: StateFlow<NetworkState> = _inforState.asStateFlow()

    fun createUser(username: String, password: String, phone: String) {
        viewModelScope.launch {
            _createState.value = NetworkState.Loading
            createUserUseCase.execute(username, password, phone).collect { state ->
                _createState.value = state
            }
        }
    }

    fun updateUser(phoneNumber: String) {
        viewModelScope.launch {
            _updateState.value = NetworkState.Loading
            updateUserUseCase.execute(phoneNumber).collect { state ->
                _updateState.value = state
            }
        }
    }

    fun getUserInfor(){
        viewModelScope.launch {
            _inforState.value = NetworkState.Loading
            getUserInformationUseCase.execute().collect { state ->
                _inforState.value = state
            }
        }
    }

}