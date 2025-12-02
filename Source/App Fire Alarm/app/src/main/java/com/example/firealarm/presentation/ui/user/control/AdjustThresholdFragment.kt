package com.example.firealarm.presentation.ui.user.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentAdjustThresholdBinding
import com.example.firealarm.presentation.utils.Constant
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdjustThresholdFragment : Fragment() {
    private var _binding: FragmentAdjustThresholdBinding? = null
    private val binding get() = _binding!!

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
            binding.checkbox.isChecked = true
            binding.inputFire.setText("${Constant.defaultFire}")
            binding.inputSmoke.setText("${Constant.defaultSmoke}")
        }

        val inputs = listOf(
            binding.inputFire,
            binding.inputSmoke
        )
        inputs.forEach {editText ->
            editText.addTextChangedListener {
                updateCheckboxSate()
                updateButtonState()
            }
        }

    }

    private fun saveThreshold (){
        findNavController().popBackStack()
    }

    private fun updateCheckboxSate() {
        val isUpdate = binding.inputFire.text.toString() != Constant.defaultFire.toString() ||
                binding.inputSmoke.text.toString() != Constant.defaultSmoke.toString()

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