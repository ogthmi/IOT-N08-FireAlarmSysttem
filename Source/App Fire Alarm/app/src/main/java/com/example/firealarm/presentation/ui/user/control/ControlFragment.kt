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
import com.example.firealarm.domain.model.Sensor
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
        viewModel.getSensor()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sensorData.collectLatest { data ->
                Log.d("----Sensor", data.toString())
                Log.d("----Current sensor", "Temperature_" + data.temperature + ", Humidity_" + data.humidity + ", Fire_" + data.fire + ", Smoke_" + data.smoke);

                // Kiểm tra xem đã nhận được dữ liệu từ thiết bị chưa (dựa vào deviceId)
                // Nếu deviceId rỗng nghĩa là chưa có dữ liệu từ Firebase
                val hasData = data.deviceId.isNotEmpty()

                if (hasData) {
                    deviceId = data.deviceId
                    // Đã có dữ liệu, hiển thị giá trị (kể cả khi là 0)
                    binding.txtTemperature.text = "${data.temperature}"
                    binding.txtHumidity.text = "${data.humidity}"
                    binding.txtFire.text = "${data.fire}"
                    binding.txtSmoke.text = "${data.smoke}"
                } else {
                    // Chưa có dữ liệu, hiển thị placeholder
                    binding.txtTemperature.text = "--"
                    binding.txtHumidity.text = "--"
                    binding.txtFire.text = "--"
                    binding.txtSmoke.text = "--"
                }

                // Kiểm tra và hiển thị cảnh báo cháy
                checkAndShowFireAlert(data)
            }
        }

        viewModel.getStatus()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statusData.collectLatest {data ->
                Log.d("----State", "Pump: " + data.pumpState + "Buzzer: " + data.buzzerState)
                // Cập nhật trạng thái máy bơm
                if(data.pumpState == "ON"){
                    binding.switchPump.isChecked = true
                    binding.txtPump.text = "Bật"
                }
                else {
                    binding.switchPump.isChecked = false
                    binding.txtPump.text = "Tắt"
                }

                // Cập nhật trạng thái còi báo
                if(data.buzzerState == "ON") {
                    binding.switchAlarm.isChecked = true
                    binding.txtAlarm.text = "Bật"
                }
                else {
                    binding.switchAlarm.isChecked = false
                    binding.txtAlarm.text = "Tắt"
                }
            }
        }
    }

    private fun checkAndShowFireAlert(sensor: Sensor) {
        context?.let { context ->
            if (sensor.isFire) {
                NotificationHelper.showFireAlert(
                    context,
                    "⚠️ PHÁT HIỆN CHÁY! Giá trị lửa: ${sensor.fire}. Vui lòng kiểm tra ngay!"
                )

                Log.d("ControlFragment", "Fire detected! Showing notification and alert border")
            }

            // Kiểm tra nếu phát hiện khói
            if (sensor.isSmoke) {
                NotificationHelper.showFireAlert(
                    context,
                    "⚠️ PHÁT HIỆN KHÓI! Giá trị khói: ${sensor.smoke}. Có thể có nguy cơ cháy!"
                )
                Log.d("ControlFragment", "Smoke detected! Showing notification and alert border")
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}