package com.example.firealarm.presentation.ui.user.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentAdjustThresholdBinding
import com.example.firealarm.domain.model.Threshold
import com.example.firealarm.presentation.utils.Constant
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdjustThresholdFragment : Fragment() {
    private var _binding: FragmentAdjustThresholdBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdjustThresholdViewModel by viewModels()
    private var fireThreshold: Double = 0.0
    private var smokeThreshold: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdjustThresholdBinding.inflate(layoutInflater, container, false)
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
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveThreshold()
        }

        binding.checkbox.setOnClickListener {
            if(binding.checkbox.isChecked == true){
                binding.inputFire.setText("${fireThreshold}")
                binding.inputSmoke.setText("${smokeThreshold}")
            }
            else{
                binding.inputFire.setText("")
                binding.inputSmoke.setText("")
            }
        }

        val inputs = listOf(
            binding.inputFire,
            binding.inputSmoke
        )
        inputs.forEach { editText ->
            editText.addTextChangedListener {
                updateButtonState()
                updateCheckboxSate()
            }
        }

    }
    
    private fun init() {
        // Observe threshold state
        viewModel.loadThresholds()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.thresholdState.collect { state ->
                when (state) {
                    is NetworkState.Loading, NetworkState.Init -> {
                    }
                    is NetworkState.Success<*> -> {
                        val listThreshold = state.data as List<Threshold>
                        listThreshold.forEach { threshold ->
                            when (threshold.sensorName) {
                                "MHS" -> {
                                    if (threshold.ruleName == "fire_threshold") {
                                        fireThreshold = threshold.threshold
                                        binding.inputFire.setText(fireThreshold.toString())
                                    }
                                }
                                "MP2" -> {
                                    if (threshold.ruleName == "smoke_threshold") {
                                        smokeThreshold = threshold.threshold
                                        binding.inputSmoke.setText(smokeThreshold.toString())
                                    }
                                }
                            }
                        }
                    }
                    is NetworkState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveThreshold() {
        val fireThreshold = binding.inputFire.text.toString().toDouble()
        val smokeThreshold = binding.inputSmoke.text.toString().toDouble()

        viewModel.updateThresholds(fireThreshold, smokeThreshold)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is NetworkState.Init, is NetworkState.Loading -> {
                        binding.btnSave.isEnabled = false
                        binding.btnSave.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                        binding.loading.visibility = View.VISIBLE
                    }
                    is NetworkState.Success<*> -> {
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Update threshold successful!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is NetworkState.Error -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateCheckboxSate() {
        val isUpdate = binding.inputFire.text.toString() != fireThreshold.toString() ||
                binding.inputSmoke.text.toString() != smokeThreshold.toString()

        if(isUpdate){
            binding.checkbox.isChecked = false
        }
        else binding.checkbox.isChecked = true
    }

    private fun updateButtonState(){
        val valid = !binding.inputFire.text.toString().isNullOrEmpty() &&
                !binding.inputSmoke.text.toString().isNullOrEmpty()

        if(valid){
            binding.btnSave.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
            binding.btnSave.isEnabled = true
        }
        else{
            binding.btnSave.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
            binding.btnSave.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }
}