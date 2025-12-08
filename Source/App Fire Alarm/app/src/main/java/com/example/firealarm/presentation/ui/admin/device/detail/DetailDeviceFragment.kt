package com.example.firealarm.presentation.ui.admin.device.detail

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentDetailDeviceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailDeviceFragment : Fragment() {
    private var _binding: FragmentDetailDeviceBinding? = null
    private val binding get() = _binding!!
    private val args: DetailDeviceFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailDeviceBinding.inflate(layoutInflater, container, false)
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