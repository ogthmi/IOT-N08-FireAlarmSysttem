package com.example.firealarm.presentation.ui.admin.firmware

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentFirmwareListBinding
import com.example.firealarm.domain.model.Firmware
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.example.firealarm.presentation.utils.AppPreferences
import kotlinx.coroutines.delay

@AndroidEntryPoint
class FirmwareListFragment : Fragment() {

    private var _binding: FragmentFirmwareListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FirmwareListViewModel by viewModels()
    private lateinit var firmwareAdapter: FirmwareAdapter
    private lateinit var listFirmware: List<Firmware>
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirmwareListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE

        setupRecyclerView()
        setupSearchView()
        setupObservers()
        
        binding.addFirmware.setOnClickListener {
            findNavController().navigate(FirmwareListFragmentDirections.actionFirmwareListFragmentToCreateFirmwareFragment(0, "", 0, "", false))
        }

        binding.btnLogout.setOnClickListener {
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

    
    private fun setupRecyclerView() {
        firmwareAdapter = FirmwareAdapter(
            onItemClick = { firmware ->
                findNavController().navigate(
                    FirmwareListFragmentDirections.actionFirmwareListFragmentToFirmwareDetailFragment(
                        firmware.version,
                        firmware.versionNumber,
                        firmware.releasedAt,
                        firmware.downloadUrl ?: "",
                        firmware.description ?: ""
                    )
                )
            },
            onEditClick = { firmware ->
                findNavController().navigate(
                    FirmwareListFragmentDirections.actionFirmwareListFragmentToCreateFirmwareFragment(
                        firmware.id,
                       firmware.version,
                        firmware.versionNumber,
                        firmware.description ?: "",
                        true
                    )
                )
            },
            onDeleteClick = { firmware ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete")
                    .setMessage("Are you want to delete firmware ${firmware.version}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteFirmware(firmware.id)
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteState.collect { state ->
                                when (state){
                                    is NetworkState.Init, is NetworkState.Loading -> {}
                                    is NetworkState.Success<*> -> {
                                        viewModel.loadFirmwareList()
                                        Toast.makeText(requireContext(), "Delete firmware successful!",
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
        binding.recyclerViewFirmware.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = firmwareAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.loadFirmwareList()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.firmwareState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        val firmwareList = state.data as? List<Firmware> ?: emptyList()
                        listFirmware = firmwareList
                        if (firmwareList.isEmpty()) {
                            binding.noFirmware.visibility = View.VISIBLE
                            binding.recyclerViewFirmware.visibility = View.INVISIBLE
                        } else {
                            binding.noFirmware.visibility = View.INVISIBLE
                            binding.recyclerViewFirmware.visibility = View.VISIBLE
                            firmwareAdapter.submitList(firmwareList)
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
            listFirmware
        } else {
            listFirmware.filter {
                it.version.contains(query, ignoreCase = true)
            }
        }
        firmwareAdapter.submitList(filtered)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}