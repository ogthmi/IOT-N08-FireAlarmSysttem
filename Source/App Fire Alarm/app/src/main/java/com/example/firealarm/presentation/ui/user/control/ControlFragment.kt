package com.example.firealarm.presentation.ui.user.control

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firealarm.databinding.FragmentControlBinding
import com.example.firealarm.domain.model.Telemetry
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.utils.Constant
import com.example.firealarm.presentation.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ControlFragment : Fragment() {
    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ControlViewModel by viewModels()
    private lateinit var deviceId: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setupHeader()

        binding.alarmBtn.setOnClickListener {
            // Lấy trạng thái hiện tại từ switch
            val currentState = if (binding.switchAlarm.isChecked) "ON" else "OFF"
            viewModel.toggleBuzzer(currentState, deviceId)
        }

        binding.pumpBtn.setOnClickListener {
            // Lấy trạng thái hiện tại từ switch
            val currentState = if (binding.switchPump.isChecked) "ON" else "OFF"
            viewModel.togglePump(currentState, deviceId)
        }

        binding.btnAdjust.setOnClickListener {
            findNavController().navigate(ControlFragmentDirections.actionControlFragmentToAdjustThresholdFragment())
        }
    }

    private fun init(){
        // Lấy deviceId từ SharedPreferences
        deviceId = AppPreferences.getDeviceId()?.split("-")[1]?.trim() ?: ""
        // Tạo notification channel
        context?.let { NotificationHelper.createNotificationChannel(it) }

        // Observe telemetry data từ WebSocket
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.telemetryData.collect { data ->
                updateSensorDisplay(data)
            }
        }

        // Observe notification từ WebSocket
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notificationData.collect { notificationMessage ->
                notificationMessage?.let { message ->
                    showNotificationFromWebSocket(message)
                    // Reset sau khi hiển thị để có thể nhận notification mới
                    viewModel.clearNotification()
                }
            }
        }

        // Observe connection status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionStatus.collect { isConnected ->
                if (!isConnected) {
                    // Hiển thị placeholder khi mất kết nối
                    binding.txtTemperature.text = "--"
                    binding.txtHumidity.text = "--"
                    binding.txtFire.text = "--"
                    binding.txtSmoke.text = "--"
                }
            }
        }
    }

    private fun showNotificationFromWebSocket(message: String) {
        context?.let { context ->
            try {
                val notificationText = parseNotificationMessage(message)

                // Hiển thị notification
                NotificationHelper.showFireAlert(
                    context,
                    notificationText
                )

                Log.d("ControlFragment", "Notification received from WebSocket: $message")
            } catch (e: Exception) {
                Log.e("ControlFragment", "Error showing notification: ${e.message}", e)
                // Fallback: hiển thị message trực tiếp
                NotificationHelper.showFireAlert(context, message)
            }
        }
    }

    private fun parseNotificationMessage(message: String): String {
        return try {
            // Thử parse JSON nếu message là JSON
            if (message.trim().startsWith("{") || message.trim().startsWith("[")) {
                val json = org.json.JSONObject(message)
                // Có thể có các trường: message, type, alertType, content, etc.
                when {
                    json.has("message") -> json.getString("message")
                    json.has("content") -> json.getString("content")
                    json.has("alertMessage") -> json.getString("alertMessage")
                    json.has("text") -> json.getString("text")
                    else -> {
                        // Nếu không có trường message, thử lấy tất cả các trường
                        val type = if (json.has("type")) json.getString("type") else ""
                        val alertType = if (json.has("alertType")) json.getString("alertType") else ""
                        val value = if (json.has("value")) json.getString("value") else ""

                        when {
                            type.equals("FIRE", ignoreCase = true) || alertType.equals("FIRE", ignoreCase = true) -> {
                                "🚨 PHÁT HIỆN CHÁY! $value"
                            }
                            type.equals("SMOKE", ignoreCase = true) || alertType.equals("SMOKE", ignoreCase = true) -> {
                                "⚠️ PHÁT HIỆN KHÓI! $value"
                            }
                            else -> message // Fallback về message gốc
                        }
                    }
                }
            } else {
                // Nếu không phải JSON, dùng trực tiếp
                message
            }
        } catch (e: Exception) {
            // Nếu parse JSON lỗi, dùng message gốc
            Log.w("ControlFragment", "Failed to parse notification as JSON, using raw message: ${e.message}")
            message
        }
    }

    private fun updateSensorDisplay(data: List<Telemetry>) {
        if(data.size == 0){
            binding.txtTemperature.text = "__"
            binding.txtHumidity.text = "__"
            binding.txtFire.text = "__"
            binding.txtSmoke.text = "__"

            binding.switchPump.isChecked = false
            binding.txtPump.text = "Tắt"
            binding.switchAlarm.isChecked = false
            binding.txtAlarm.text = "Tắt"
            return
        }
        if(data.get(0).deviceId.equals(deviceId)) {
            for (item: Telemetry in data) {
                if (item.name.equals(Constant.dht22t)) binding.txtTemperature.text =
                    "${item.value} ${item.unit}"
                if (item.name.equals(Constant.dht22h)) binding.txtHumidity.text =
                    "${item.value} ${item.unit}"
                if (item.name.equals(Constant.mp2)) binding.txtFire.text = "${item.value}"
                if (item.name.equals(Constant.mhs)) binding.txtSmoke.text = "${item.value}"
                if (item.name.equals(Constant.pump)) {
                    binding.switchPump.isChecked = item?.status ?: false
                    binding.txtPump.text = if (item.status == true) "Bật" else "Tắt"
                }
                if (item.name.equals(Constant.buzzer)) {
                    binding.switchAlarm.isChecked = item?.status ?: false
                    binding.txtAlarm.text = if (item.status == true) "Bật" else "Tắt"
                }
            }
        }
    }

    private fun setupHeader() {
        val username = AppPreferences.getUsername()
        binding.textUsername.text = username ?: "Unknown User"

        // Lấy device ID từ SharedPreferences
        val deviceId = AppPreferences.getDeviceId()
        if (deviceId != null) {
            binding.textDeviceId.text = "Device: $deviceId"
        } else {
            binding.textDeviceId.text = "---"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}