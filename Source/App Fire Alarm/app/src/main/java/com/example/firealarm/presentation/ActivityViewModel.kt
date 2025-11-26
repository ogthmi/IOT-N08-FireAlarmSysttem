package com.example.firealarm.presentation

import android.util.Log
import android.util.Printer
import androidx.lifecycle.ViewModel
import com.example.firealarm.domain.usecase.GetSensorDataUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
): ViewModel(){
}