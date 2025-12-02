package com.example.firealarm.presentation.ui.admin.user.create

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentCreateDeviceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateDeviceFragment : Fragment() {

    private var _binding: FragmentCreateDeviceBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDeviceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}