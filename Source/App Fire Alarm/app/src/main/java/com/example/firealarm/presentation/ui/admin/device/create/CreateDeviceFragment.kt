package com.example.firealarm.presentation.ui.admin.device.create

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firealarm.R
import com.example.firealarm.data.model.device.DeviceResponse
import com.example.firealarm.databinding.FragmentCreateDeviceBinding
import com.example.firealarm.domain.model.Device
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateDeviceFragment : Fragment() {

    private var _binding: FragmentCreateDeviceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateDeviceViewModel by viewModels()
    private val args: CreateDeviceFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDeviceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        val inputs = listOf(
            binding.inputDeviceId,
            binding.inputDeviceName,
            binding.inputFireThreshold,
            binding.inputSmokeThreshold
        )

        inputs.forEach {editText ->
            editText.addTextChangedListener {
                updateButtonState()
            }
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCreate.setOnClickListener {
            if(args.edit) updateDevice()
            else createDevice()
        }
    }

    private fun init(){
        if(args.edit){
            binding.title.text = "Edit device"
            binding.btnCreate.text = "Save"

            binding.inputDeviceId.setText(args.deviceId)
            binding.inputDeviceId.background =  ContextCompat.getDrawable(
                requireContext(),
                R.drawable.input_blur
            )
            binding.inputDeviceId.isEnabled = false

            binding.inputDeviceName.setText(args.deviceName)
            binding.inputDescription.setText(args.des ?: "")
            binding.inputFireThreshold.setText(args.fireThreshold.toString())
            binding.inputSmokeThreshold.setText(args.smokeThreshold.toString())
            binding.btnCreate.background =  ContextCompat.getDrawable(
                requireContext(),
                R.drawable.button
            )
        }
    }
    private fun updateButtonState(){
        val valid = !binding.inputDeviceId.text.toString().isNullOrEmpty() &&
                !binding.inputDeviceName.text.toString().isNullOrEmpty() &&
                !binding.inputFireThreshold.text.toString().isNullOrEmpty() &&
                !binding.inputSmokeThreshold.text.toString().isNullOrEmpty()

        if(valid){
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
            binding.btnCreate.isEnabled = true
        }
        else{
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
            binding.btnCreate.isEnabled = false
        }
    }

    private fun createDevice(){
        val deviceId = binding.inputDeviceId.text.toString()
        val deviceName = binding.inputDeviceName.text.toString()
        val des = binding.inputDescription.text.toString()
        val fireThreshold = binding.inputFireThreshold.text.toString().toDouble()
        val smokeThreshold = binding.inputSmokeThreshold.text.toString().toDouble()

        viewModel.createDevice(deviceId = deviceId, deviceName =  deviceName, description = des, userId =  args.userId,
            fireThreshold = fireThreshold, smokeThreshold = smokeThreshold)
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.createState.collect {  state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Create device successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }

                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateDevice(){
        val deviceId = args.deviceId
        val deviceName = binding.inputDeviceName.text.toString().trim()
        val description = binding.inputDescription.text.toString().trim()
        val fireThreshold = binding.inputFireThreshold.text.toString().toDoubleOrNull() ?: 0.0
        val smokeThreshold = binding.inputSmokeThreshold.text.toString().toDoubleOrNull() ?: 0.0

        viewModel.updateDevice(
            deviceId = deviceId,
            deviceName = deviceName,
            description = if (description.isEmpty()) null else description,
            smokeThreshold = smokeThreshold,
            fireThreshold = fireThreshold
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnCreate.isEnabled = false
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Update device successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
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