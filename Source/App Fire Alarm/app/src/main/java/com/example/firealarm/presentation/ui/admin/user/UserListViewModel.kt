package com.example.firealarm.presentation.ui.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.model.UserInfo
import com.example.firealarm.domain.repository.UserRepository
import com.example.firealarm.domain.usecase.DeleteUserUseCase
import com.example.firealarm.domain.usecase.GetListUserUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getListUserUseCase: GetListUserUseCase,
    private val deteteUserUseCase: DeleteUserUseCase
): ViewModel() {

    private val _usersState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val usersState: StateFlow<NetworkState> = _usersState.asStateFlow()

    private val _deleteState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val deleteState: StateFlow<NetworkState> = _deleteState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = NetworkState.Loading
            getListUserUseCase.execute().collect { state ->
                _usersState.value = state
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _deleteState.value = NetworkState.Loading
            deteteUserUseCase.execute(userId).collect { state ->
                _deleteState.value = state
            }
        }
    }
}