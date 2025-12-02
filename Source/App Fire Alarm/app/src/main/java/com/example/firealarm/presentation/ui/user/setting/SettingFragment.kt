package com.example.firealarm.presentation.ui.user.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentSettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE

        binding.changeDevice.setOnClickListener {
            findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToChooseDeviceFragment2("fromSetting"))
        }

        binding.updateFirmware.setOnClickListener {
            findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToUpdateFirmwareFragment())
        }

        binding.logout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(3000)
            }

            val navController = findNavController()
            navController.setGraph(R.navigation.auth_nav_graph)

        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}