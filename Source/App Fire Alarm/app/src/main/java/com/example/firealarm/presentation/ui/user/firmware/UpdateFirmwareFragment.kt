package com.example.firealarm.presentation.ui.user.firmware

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentUpdateFirmwareBinding
import com.example.firealarm.domain.model.Device
import com.example.firealarm.domain.model.Firmware
import com.example.firealarm.domain.model.OtaHistory
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateFirmwareFragment : Fragment() {
    private var _binding: FragmentUpdateFirmwareBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UpdateFirmwareViewModel by viewModels()
    private var selectedVersion: String? = null
    private var deviceId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateFirmwareBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        init()
        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            cancelOtaUpdate()
        }

        binding.btnUpdate.setOnClickListener {
            startOtaUpdate()
        }
    }

    private fun init(){
        binding.valueDeviceId.text = AppPreferences.getDeviceId()?.trim()?.split("-")[1]?.trim()
        binding.valueDeviceName.text = AppPreferences.getDeviceId()?.trim()?.split("-")[0]?.trim()
        deviceId = AppPreferences.getDeviceId()?.trim()?.split("-")[1]?.trim()
        loadFirmwareVersion()
        loadOtaHistory()
    }

    private fun loadFirmwareVersion(){
        viewModel.loadFirmwareList()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.firmwareState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                        binding.btnUpdate.isEnabled = false
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        val firmwareList = state.data as? List<Firmware> ?: emptyList()
                        if (firmwareList.isNotEmpty()) {
                            binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
                            binding.btnUpdate.isEnabled = true
                            setupSpinner(firmwareList)
                        } else {
                            binding.spinner.visibility = View.GONE
                            binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                            binding.btnUpdate.isEnabled = false
                            Toast.makeText(requireContext(), "No firmware version found.", Toast.LENGTH_SHORT).show()
                        }

                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupSpinner(firmwares: List<Firmware>) {
        val versions = firmwares.map { it.version }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            versions
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spinner.adapter = adapter
        binding.spinner.setSelection(0)
        selectedVersion = versions[0]

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedVersion = versions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun loadOtaHistory() {
        if (deviceId.isNullOrEmpty()) return

        viewModel.loadOtaHistory(deviceId!!)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.otaHistoryState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        // Don't show loading for history, it's background load
                    }
                    is NetworkState.Success<*> -> {
                        val historyList = state.data as? List<OtaHistory> ?: emptyList()
                        updateFirmwareInfo(historyList)
                    }
                    is NetworkState.Error -> {
                        // Silently fail, don't show error for history
                    }
                }
            }
        }
    }

    private fun updateFirmwareInfo(historyList: List<OtaHistory>) {
        if (historyList.isEmpty()) {
            binding.valueFirmwareCurrent.text = "Unknown"
            binding.valueStatus.visibility = View.GONE
            binding.titleStatus.visibility = View.GONE
            return
        }

        val completedUpdates = historyList.filter { it.status == "COMPLETED" }
        val latestCompletedUpdate = completedUpdates.maxByOrNull { it.id }

        if (latestCompletedUpdate != null) {
            binding.valueFirmwareCurrent.text = latestCompletedUpdate.nextVersion
        } else {
            // Nếu không có COMPLETED, lấy previousVersion từ record mới nhất
            val latestUpdate = historyList.maxByOrNull { it.id }
            if (latestUpdate != null && latestUpdate.previousVersion != null && latestUpdate.previousVersion != "Unknown") {
                binding.valueFirmwareCurrent.text = latestUpdate.previousVersion
            } else {
                binding.valueFirmwareCurrent.text = "Unknown"
            }
        }

        // Find current status (IN_PROGRESS first, then latest status)
        val lastedStatus = historyList[0].status
        if (lastedStatus == "IN_PROGRESS") {
            binding.valueStatus.text = "IN_PROGRESS"
            binding.valueStatus.visibility = View.VISIBLE
            binding.titleStatus.visibility = View.VISIBLE
            binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
            binding.btnUpdate.isEnabled = false

            binding.spinner.visibility = View.INVISIBLE
            binding.valueFirmwareUpdate.visibility = View.VISIBLE
            binding.valueFirmwareUpdate.text = historyList[0].nextVersion

            binding.titleChooseFirmware.text = "Version firmware next"
        }
        else if(lastedStatus == "COMPLETED"){
            binding.valueStatus.visibility = View.GONE
            binding.titleStatus.visibility = View.GONE
            binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
            binding.btnUpdate.isEnabled = true
        }
        else {
            binding.valueStatus.visibility = View.GONE
            binding.titleStatus.visibility = View.GONE
        }
    }

    private fun startOtaUpdate() {
        if (selectedVersion.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a firmware version", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.startOtaUpdate(deviceId ?: "", selectedVersion!!)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.otaUpdateState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnUpdate.isEnabled = false
                        binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "OTA update initiated successfully!", Toast.LENGTH_SHORT).show()
                        // Reload history to show new status
                        loadOtaHistory()
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnUpdate.isEnabled = true
                        binding.btnUpdate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun cancelOtaUpdate(){
        viewModel.cancelOtaUpdate(deviceId = deviceId ?: "")
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cancelOtaState.collect{ state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnCancel.isEnabled = false
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCancel.isEnabled = true
                        Toast.makeText(requireContext(), "OTA update cancel successfully!", Toast.LENGTH_SHORT).show()

                        findNavController().popBackStack()
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCancel.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}