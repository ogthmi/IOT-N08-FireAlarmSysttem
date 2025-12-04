package com.example.firealarm.presentation.ui.admin.device

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentListDeviceBinding
import com.example.firealarm.domain.model.Device
import com.example.firealarm.domain.model.UserInfo
import com.example.firealarm.presentation.ui.admin.user.UserListFragmentDirections
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListDeviceFragment : Fragment() {
    private var _binding: FragmentListDeviceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListDeviceViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter
    val args: ListDeviceFragmentArgs by navArgs()
    private lateinit var listDevice: List<Device>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListDeviceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        val userId = args.userId
        setupRecyclerView()
        setupSearchView()
        setupObservers(userId)

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.addDevice.setOnClickListener {
            findNavController().navigate(ListDeviceFragmentDirections.actionListDeviceFragmentToCreateDeviceFragment2(
                userId, "", "", "", 0f, 0f, false))
        }
    }
    
    private fun setupRecyclerView() {
        val userId = args.userId
        deviceAdapter = DeviceAdapter(
            onItemClick = { device ->
                findNavController().navigate(ListDeviceFragmentDirections.actionListDeviceFragmentToDeviceDetailFragment2(
                    device.deviceId, device.deviceName, device.description,
                    device.thresholds[0].threshold.toFloat(), device.thresholds[1].threshold.toFloat()
                ))
            },
            onEditClick = { device ->
                findNavController().navigate(
                    ListDeviceFragmentDirections.actionListDeviceFragmentToCreateDeviceFragment2(
                        userId, device.deviceId, device.deviceName, device.description,
                        device.thresholds[0].threshold.toFloat(), device.thresholds[1].threshold.toFloat(),
                        true
                    )
                )
            },
            onDeleteClick = { device ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete")
                    .setMessage("Are you want to delete ${device.deviceName}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteDevice(device.deviceId)
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteState.collect { state ->
                                when (state){
                                    is NetworkState.Init, is NetworkState.Loading -> {}
                                    is NetworkState.Success<*> -> {
                                        viewModel.loadDevices(userId)
                                        Toast.makeText(requireContext(), "Delete device successful!",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    is NetworkState.Error -> {
                                        Toast.makeText(requireContext(), state.message,
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText ?: "")
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        val filtered = if (query.isBlank()) {
            listDevice
        } else {
            listDevice.filter {
                it.deviceName.contains(query, ignoreCase = true)
            }
        }
        deviceAdapter.submitList(filtered)
    }
    
    private fun setupObservers(userId: Int) {
        viewModel.loadDevices(userId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deviceState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        val devices = state.data as? List<Device> ?: emptyList()
                        listDevice = devices

                        if(devices.isEmpty()) {
                            binding.noDevice.visibility = View.VISIBLE
                            binding.recyclerViewDevices.visibility = View.INVISIBLE
                        }
                        else{
                            binding.noDevice.visibility = View.INVISIBLE
                            binding.recyclerViewDevices.visibility = View.VISIBLE
                            deviceAdapter.submitList(devices)
                        }
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
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