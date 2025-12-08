package com.example.firealarm

import android.app.Application
import android.util.Log
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.service.FireAlarmMonitoringService
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(context = this)
        FireAlarmMonitoringService.startService(this)
    }
}