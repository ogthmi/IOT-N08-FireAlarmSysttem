package com.example.firealarm.presentation.ui.user.statistic

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.firealarm.R
import com.example.firealarm.data.model.statistic.NotificationStatisticResponse
import com.example.firealarm.data.model.statistic.SensorStatisticResponse
import com.example.firealarm.databinding.FragmentStatisticBinding
import com.example.firealarm.domain.model.SensorStatistic
import com.example.firealarm.domain.model.NotificationStatistic
import com.example.firealarm.data.model.statistic.toDomain
import com.example.firealarm.data.model.statistic.toDomain as notificationToDomain
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.utils.NetworkState
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticViewModel by viewModels()
    private var deviceId: String? = null
    private var currentTab: Int = 0 // 0: Sensor, 1: Notification

    companion object {
        // Giới hạn số điểm dữ liệu tối đa hiển thị trên biểu đồ
        private const val MAX_DATA_POINTS = 120
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE

        init()
        setupTabLayout()
        setupObservers()
    }

    private fun init() {
        deviceId = AppPreferences.getDeviceId()?.trim()?.split("-")?.getOrNull(1)?.trim()

        if (deviceId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Device ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Load sensor statistics by default
        viewModel.loadSensorStatistics(deviceId!!)
    }

    private fun setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Telemetry"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Notification"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                if (deviceId.isNullOrEmpty()) return

                when (currentTab) {
                    0 -> {
                        // Sensor tab
                        viewModel.loadSensorStatistics(deviceId!!)
                    }
                    1 -> {
                        // Notification tab
                        viewModel.loadNotificationStatistics(deviceId!!)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        // Sensor statistics observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sensorStatisticState.collect { state ->
                when (state) {
                    is NetworkState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        hideAllCharts()
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        val data = state.data as? SensorStatisticResponse
                        if (data != null && data.result != null) {
                            val temperatures = data.result.temperatures.map { it.toDomain() }
                            val smokes = data.result.smokes.map { it.toDomain() }
                            val humidities = data.result.humidities.map { it.toDomain() }

                            if (currentTab == 0) {
                                displaySensorCharts(temperatures, smokes, humidities)
                            }
                        }
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is NetworkState.Init -> {}
                }
            }
        }

        // Notification statistics observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notificationStatisticState.collect { state ->
                when (state) {
                    is NetworkState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        hideAllCharts()
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        val data = state.data as? NotificationStatisticResponse
                        if (data != null && data.result != null) {
                            val fireNotifications = data.result.fireNotifications.map { it.notificationToDomain() }
                            val smokeNotifications = data.result.smokeNotifications.map { it.notificationToDomain() }

                            if (currentTab == 1) {
                                displayNotificationCharts(fireNotifications, smokeNotifications)
                            }
                        }
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is NetworkState.Init -> {}
                }
            }
        }
    }

    private fun displaySensorCharts(temperatures: List<SensorStatistic>, smokes: List<SensorStatistic>, humidities: List<SensorStatistic>) {
        hideAllCharts()

        if (temperatures.isNotEmpty()) {
            binding.titleTemperature.visibility = View.VISIBLE
            binding.chartTemperature.visibility = View.VISIBLE
            setupLineChart(
                binding.chartTemperature,
                temperatures.map { it.value.toFloatOrNull() ?: 0f },
                temperatures.map { formatDateTime(it.createdAt) },
                "Temperature (°C)",
                ContextCompat.getColor(requireContext(), R.color.red)
            )
        }

        if (smokes.isNotEmpty()) {
            binding.titleSmoke.visibility = View.VISIBLE
            binding.chartSmoke.visibility = View.VISIBLE
            setupLineChart(
                binding.chartSmoke,
                smokes.map { it.value.toFloatOrNull() ?: 0f },
                smokes.map { formatDateTime(it.createdAt) },
                "Smoke",
                ContextCompat.getColor(requireContext(), R.color.green)
            )
        }

        if (humidities.isNotEmpty()) {
            binding.titleHumidity.visibility = View.VISIBLE
            binding.chartHumidity.visibility = View.VISIBLE
            setupLineChart(
                binding.chartHumidity,
                humidities.map { it.value.toFloatOrNull() ?: 0f },
                humidities.map { formatDateTime(it.createdAt) },
                "Humidity",
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
        }

        if(smokes.isNullOrEmpty() && humidities.isNullOrEmpty() && temperatures.isNullOrEmpty()){
            binding.noValue.visibility = View.VISIBLE
        }
        else binding.noValue.visibility = View.INVISIBLE
    }

    private fun displayNotificationCharts(
        fireNotifications: List<NotificationStatistic>,
        smokeNotifications: List<NotificationStatistic>
    ) {
        hideAllCharts()

        if (fireNotifications.isNotEmpty()) {
            binding.titleFireNotification.visibility = View.VISIBLE
            binding.chartFireNotification.visibility = View.VISIBLE
            setupLineChart(
                binding.chartFireNotification,
                fireNotifications.map { it.value.toFloat() },
                fireNotifications.map { formatDateTime(it.time) },
                "Fire notification",
                ContextCompat.getColor(requireContext(), R.color.red)
            )
        }

        if (smokeNotifications.isNotEmpty()) {
            binding.titleSmokeNotification.visibility = View.VISIBLE
            binding.chartSmokeNotification.visibility = View.VISIBLE
            setupLineChart(
                binding.chartSmokeNotification,
                smokeNotifications.map { it.value.toFloat() },
                smokeNotifications.map { formatDateTime(it.time) },
                "Smoke notification",
                ContextCompat.getColor(requireContext(), R.color.gray)
            )
        }

        if(fireNotifications.isNullOrEmpty() && smokeNotifications.isNullOrEmpty()){
            binding.noValue.visibility = View.VISIBLE
        }
        else binding.noValue.visibility = View.INVISIBLE
    }

    private fun setupLineChart(
        chart: LineChart,
        values: List<Float>,
        labels: List<String>,
        label: String,
        color: Int
    ) {
        // Giới hạn số điểm dữ liệu để tránh quá tải
        val (limitedValues, limitedLabels) = if (values.size > MAX_DATA_POINTS) {
            // Nếu vượt quá giới hạn, chỉ lấy các điểm mới nhất
            Pair(values.takeLast(MAX_DATA_POINTS), labels.takeLast(MAX_DATA_POINTS))
        } else {
            Pair(values, labels)
        }

        val entries = limitedValues.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            valueTextColor = color
            lineWidth = 2f
            setCircleColor(color)
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        val lineData = LineData(dataSet)
        chart.data = lineData

        // Configure chart appearance
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        // Configure X-axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        xAxis.textSize = 10f
        xAxis.labelRotationAngle = -45f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < limitedLabels.size) {
                    limitedLabels[index]
                } else {
                    ""
                }
            }
        }

        // Giới hạn số label hiển thị trên trục X để tránh chồng chéo
        xAxis.setLabelCount(minOf(limitedLabels.size, 10), true)

        // Configure Y-axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.gray)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        chart.invalidate()
    }

    private fun formatDateTime(dateTimeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateTimeString)
            date?.let { outputFormat.format(it) } ?: dateTimeString
        } catch (e: Exception) {
            dateTimeString
        }
    }

    private fun hideAllCharts() {
        binding.titleTemperature.visibility = View.GONE
        binding.chartTemperature.visibility = View.GONE
        binding.titleSmoke.visibility = View.GONE
        binding.chartSmoke.visibility = View.GONE
        binding.titleHumidity.visibility = View.GONE
        binding.chartHumidity.visibility = View.GONE
        binding.titleFireNotification.visibility = View.GONE
        binding.chartFireNotification.visibility = View.GONE
        binding.titleSmokeNotification.visibility = View.GONE
        binding.chartSmokeNotification.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
