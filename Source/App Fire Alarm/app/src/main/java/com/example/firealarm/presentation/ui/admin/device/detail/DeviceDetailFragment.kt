package com.example.firealarm.presentation.ui.admin.device.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firealarm.databinding.FragmentDeviceDetailBinding
import com.example.firealarm.domain.model.Telemetry
import com.example.firealarm.presentation.utils.Constant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceDetailFragment : Fragment() {
    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DeviceDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupViews() {
        binding.textDeviceId.setText(args.deviceId)
        binding.textDeviceName.setText(args.deviceName)
        binding.textDescription.setText(args.description)
        binding.titleSmokeThreshold.setText("Smoke sensor threshold: ${args.smokeThreshold}")
        binding.titleFireThreshold.setText("Fire sensor threshold: ${args.fireThreshold}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}