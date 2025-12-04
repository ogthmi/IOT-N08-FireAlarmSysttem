package com.example.firealarm.presentation.ui.user.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentHomeBinding
import com.example.firealarm.presentation.utils.AppPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE

        setupHeader()
        
        binding.btnCallRoot.setOnClickListener {
            requestPermission()
        }
    }
    
    private fun setupHeader() {
        val username = AppPreferences.getUsername()
        binding.textUsername.text = username ?: "Unknown User"
        
        // Lấy device ID từ SharedPreferences
        val deviceId = AppPreferences.getDeviceId()
        if (deviceId != null) {
            binding.textDeviceId.text = "Device: $deviceId"
        } else {
            binding.textDeviceId.text = "---"
        }
    }
    private fun requestPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE), 1)
        }
        callPhone()
    }

    private fun callPhone(){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:114")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}