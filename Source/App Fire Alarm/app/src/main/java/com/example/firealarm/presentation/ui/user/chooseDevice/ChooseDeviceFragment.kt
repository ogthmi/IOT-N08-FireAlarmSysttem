package com.example.firealarm.presentation.ui.user.chooseDevice

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
import androidx.navigation.fragment.navArgs
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentChooseDeviceBinding
import com.example.firealarm.domain.model.Device
import com.example.firealarm.presentation.MainActivity
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChooseDeviceFragment : Fragment() {
    private var _binding: FragmentChooseDeviceBinding? = null
    private val binding get() = _binding!!
    private val args: ChooseDeviceFragmentArgs by navArgs()
    private val viewModel: ChooseDeviceViewModel by viewModels()
    private var selectedDevice: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseDeviceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        val mes = args.mes
        if(mes.equals("fromSetting")){
            binding.title.setText("Change device")
            binding.btnContinue.setText("Change")
            binding.closeBtn.visibility = View.VISIBLE
        }

        // Load devices from API
        viewModel.loadDevices()
        observeDevicesState()

        binding.btnContinue.setOnClickListener {
            if (selectedDevice != null) {
                AppPreferences.saveDeviceId(selectedDevice!!)
                if(mes.equals("fromSetting")) {
                    findNavController().navigate(
                        ChooseDeviceFragmentDirections.actionChooseDeviceFragment2ToHomeFragment()
                    )
                } else {
                    (requireActivity() as MainActivity).switchToMainUserGraph()
                }
            } else {
                Toast.makeText(requireContext(), "Please choose device!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeDevicesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.devicesState.collect { state ->
                when (state) {
                    is NetworkState.Init, is NetworkState.Loading -> {
                        binding.spinner.visibility = View.GONE
                        binding.loading.visibility = View.VISIBLE
                        binding.btnContinue.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                        binding.btnContinue.isEnabled = false
                    }
                    is NetworkState.Success<*> -> {
                        binding.spinner.visibility = View.VISIBLE
                        binding.loading.visibility = View.GONE
                        binding.btnContinue.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
                        binding.btnContinue.isEnabled = true
                        val devices = state.data as List<Device>
                        if (devices.isNotEmpty()) {
                            setupSpinner(devices)
                        } else {
                            binding.spinner.visibility = View.GONE
                            binding.btnContinue.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                            binding.btnContinue.isEnabled = false
                            Toast.makeText(requireContext(), "No devices.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is NetworkState.Error -> {
                        Toast.makeText(requireContext(), "Error when loading devices!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupSpinner(devices: List<Device>) {
        val deviceNames = devices.map { "${it.deviceName} - ${it.deviceId}"}

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            deviceNames
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spinner.adapter = adapter
        binding.spinner.setSelection(0)
        selectedDevice = deviceNames[0]

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDevice = "${devices[position].deviceName} - ${devices[position].deviceId}"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}