package com.example.firealarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firealarm.R
import com.example.firealarm.domain.model.Sensor
import com.example.firealarm.domain.usecase.GetSensorDataUseCase
import com.example.firealarm.presentation.MainActivity
import com.example.firealarm.presentation.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FireAlarmMonitoringService : Service() {

    @Inject
    lateinit var getSensorDataUseCase: GetSensorDataUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val CHANNEL_ID = "fire_monitoring_channel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "FireAlarmService"
        
        const val ACTION_FIRE_ALERT = "com.example.firealarm.ACTION_FIRE_ALERT"
        const val ACTION_DISMISS_ALERT = "com.example.firealarm.ACTION_DISMISS_ALERT"
        const val EXTRA_ALERT_TYPE = "alert_type"
        const val ALERT_TYPE_FIRE = "fire"
        const val ALERT_TYPE_SMOKE = "smoke"
        
        fun startService(context: Context) {
            val intent = Intent(context, FireAlarmMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, FireAlarmMonitoringService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        
        // Khởi tạo notification channel cho cảnh báo
        NotificationHelper.createNotificationChannel(this)
        
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Service sẽ tự động khởi động lại nếu bị kill
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        stopMonitoring()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Giám sát cảnh báo cháy",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service đang chạy ngầm để giám sát cảnh báo cháy"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang giám sát cảnh báo cháy")
            .setContentText("Dịch vụ đang chạy ngầm để phát hiện cháy và khói")
            .setSmallIcon(R.drawable.ic_fire)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startMonitoring() {
        Log.d(TAG, "Starting sensor monitoring")
        getSensorDataUseCase.execute()
            .onEach { sensor ->
                Log.d(TAG, "Sensor data received: Fire=${sensor.isFire}, Smoke=${sensor.isSmoke}")
                checkAndShowFireAlert(sensor)
            }
            .catch { exception ->
                Log.e(TAG, "Error monitoring sensor data: ${exception.message}", exception)
            }
            .launchIn(serviceScope)
        Log.d(TAG, "Sensor monitoring started")
    }

    private fun stopMonitoring() {
        serviceScope.cancel()
        Log.d(TAG, "Stopped monitoring sensor data")
    }

    private fun checkAndShowFireAlert(sensor: Sensor) {
        // Kiểm tra nếu phát hiện cháy
        if (sensor.isFire) {
            NotificationHelper.showFireAlert(
                this,
                "⚠️ PHÁT HIỆN CHÁY! Giá trị lửa: ${sensor.fire}. Vui lòng kiểm tra ngay!"
            )
            
            Log.d(TAG, "Fire detected! Showing notification and alert border")
        }
        // Kiểm tra nếu phát hiện khói
        if (sensor.isSmoke) {
            NotificationHelper.showFireAlert(
                this,
                "⚠️ PHÁT HIỆN KHÓI! Giá trị khói: ${sensor.smoke}. Có thể có nguy cơ cháy!"
            )
            Log.d(TAG, "Smoke detected! Showing notification and alert border")
        }
    }
}

