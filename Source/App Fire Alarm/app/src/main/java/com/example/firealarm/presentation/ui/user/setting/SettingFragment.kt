package com.example.firealarm.presentation.ui.user.setting

import android.app.AlertDialog
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
import com.example.firealarm.presentation.ui.user.setting.SettingFragmentDirections
import com.example.firealarm.presentation.utils.AppPreferences
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

        binding.changeInfo.setOnClickListener {
            findNavController().navigate(
                SettingFragmentDirections.actionSettingFragmentToCreateUserFragment2(
                    true
                )
            )
        }

        binding.changeDevice.setOnClickListener {
            findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToChooseDeviceFragment2("fromSetting"))
        }

        binding.updateFirmware.setOnClickListener {
            findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToUpdateFirmwareFragment())
        }

        binding.logout.setOnClickListener {
            AppPreferences.saveToken("")
            AlertDialog.Builder(context)
                .setTitle("Logout")
                .setMessage("Are you want to logout")
                .setPositiveButton("Logout") { dialog, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(3000)
                    }

                    val navController = findNavController()
                    navController.setGraph(R.navigation.auth_nav_graph)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}