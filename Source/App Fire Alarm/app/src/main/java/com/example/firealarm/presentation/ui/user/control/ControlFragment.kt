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
//            viewModel.toggleBuzzer(currentState, deviceId)
        }

        binding.pumpBtn.setOnClickListener {
            // Lấy trạng thái hiện tại từ switch
            val currentState = if (binding.switchPump.isChecked) "ON" else "OFF"
//            viewModel.togglePump(currentState, deviceId)
        }

        binding.btnAdjust.setOnClickListener {
            findNavController().navigate(ControlFragmentDirections.actionControlFragmentToAdjustThresholdFragment())
        }
    }

    private fun init(){
        // Lấy deviceId từ SharedPreferences
        deviceId = AppPreferences.getDeviceId() ?: ""
        
        // Observe telemetry data từ WebSocket
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.telemetryData.collect { data ->
                updateSensorDisplay(data)
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

    private fun updateSensorDisplay(data: List<Telemetry>) {
        for (item: Telemetry in data){
            if(item.name.equals(Constant.dht22t)) binding.txtTemperature.text = "${item.value} ${item.unit}"
            if(item.name.equals(Constant.dht22h)) binding.txtHumidity.text = "${item.value} ${item.unit}"
            if(item.name.equals(Constant.mp2)) binding.txtFire.text = "${item.value}"
            if(item.name.equals(Constant.mhs)) binding.txtSmoke.text = "${item.value}"
            if(item.name.equals(Constant.pump)){
                binding.switchPump.isChecked = item?.status ?: false
                binding.txtPump.text = if(item.status == true) "Bật" else "Tắt"
            }
            if(item.name.equals(Constant.buzzer)) {
                binding.switchAlarm.isChecked = item?.status ?: false
                binding.txtAlarm.text = if (item.status == true) "Bật" else "Tắt"
            }
        }
    }

//
//
//    private fun checkAndShowFireAlert(sensor: Sensor) {
//        context?.let { context ->
////            if (sensor.isFire) {
////                NotificationHelper.showFireAlert(
//                    context,
//                    "⚠️ PHÁT HIỆN CHÁY! Giá trị lửa: ${sensor.fire}. Vui lòng kiểm tra ngay!"
//                )
//
//                Log.d("ControlFragment", "Fire detected! Showing notification and alert border")
//            }
//
//            // Kiểm tra nếu phát hiện khói
//            if (sensor.isSmoke) {
//                NotificationHelper.showFireAlert(
//                    context,
//                    "⚠️ PHÁT HIỆN KHÓI! Giá trị khói: ${sensor.smoke}. Có thể có nguy cơ cháy!"
//                )
//                Log.d("ControlFragment", "Smoke detected! Showing notification and alert border")
//            }
//        }
//    }

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