package com.example.firealarm.presentation.ui.admin.user

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.widget.EditText
import android.widget.Toast
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
import com.example.firealarm.databinding.FragmentUserListBinding
import com.example.firealarm.domain.model.UserInfo
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.appcompat.widget.SearchView
import com.example.firealarm.presentation.utils.AppPreferences
import kotlinx.coroutines.delay

@AndroidEntryPoint
class UserListFragment : Fragment() {
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserListViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter
    private lateinit var listUser: List<UserInfo>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE

        setupRecyclerView()
        setupSearchView()
        setupObservers()

        binding.addDevice.setOnClickListener {
            findNavController().navigate(UserListFragmentDirections.actionUserListFragmentToCreateUserFragment(
                false))
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
        userAdapter = UserAdapter(
            onItemClick = { user ->
                findNavController().navigate(UserListFragmentDirections.actionUserListFragmentToListDeviceFragment(user.id))
            },
            onDeleteClick = { user ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete")
                    .setMessage("Are you want to delete ${user.username}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteUser(user.id)
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteState.collect { state ->
                                when (state){
                                    is NetworkState.Init, is NetworkState.Loading -> {}
                                    is NetworkState.Success<*> -> {
                                        viewModel.loadUsers()
                                        Toast.makeText(requireContext(), "Delete user successful!",
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
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
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
            listUser
        } else {
            listUser.filter {
                it.username.contains(query, ignoreCase = true)
            }
        }
        userAdapter.submitList(filtered)
    }

    private fun setupObservers() {
        viewModel.loadUsers()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usersState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        val users = state.data as? List<UserInfo> ?: emptyList()
                        listUser = users
                        if(users.isEmpty()) {
                            binding.noUser.visibility = View.VISIBLE
                            binding.recyclerViewUsers.visibility = View.INVISIBLE
                        }
                        else{
                            binding.noUser.visibility = View.INVISIBLE
                            binding.recyclerViewUsers.visibility = View.VISIBLE
                            userAdapter.submitList(users)
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