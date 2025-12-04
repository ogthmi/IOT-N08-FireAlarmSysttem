package com.example.firealarm.presentation.ui.admin.user.create

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
import androidx.navigation.fragment.navArgs
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentCreateUserBinding
import com.example.firealarm.domain.model.UserInfo
import com.example.firealarm.presentation.ui.admin.device.create.CreateDeviceFragmentArgs
import com.example.firealarm.presentation.utils.Constant
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateUserFragment : Fragment() {
    private var _binding: FragmentCreateUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateUserViewModel by viewModels()
    private val args: CreateUserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateUserBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        init()

        val inputs = listOf(
            binding.inputUsername,
            binding.inputPassword
        )

        inputs.forEach {editText ->
            editText.addTextChangedListener {
                updateButtonState()
            }
        }

        binding.btnCreate.setOnClickListener {
            if (args.edit) {
                updateUser()
            } else {
                createUser()
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun init(){
        if(args.edit){
            binding.title.text = "Edit user"
            binding.btnCreate.text = "Save"
            binding.inputUsername.background =  ContextCompat.getDrawable(
                requireContext(),
                R.drawable.input_blur
            )
            binding.inputUsername.isEnabled = false

            binding.inputPassword.setText("**********")
            binding.inputPassword.background =  ContextCompat.getDrawable(
                requireContext(),
                R.drawable.input_blur
            )
            binding.inputPassword.isEnabled = false

            viewModel.getUserInfor()
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.inforState.collect { state ->
                    when(state){
                        is NetworkState.Loading, is NetworkState.Init -> {}
                        is NetworkState.Success<*> -> {
                            val user = state.data as UserInfo
                            binding.btnCreate.isEnabled = true
                            binding.btnCreate.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.button
                            )
                            binding.inputUsername.setText(user.username)
                            binding.inputPhoneNumber.setText(user.phone)
                            binding.inputPasswordLayout.isEndIconVisible = false

                        }
                        is NetworkState.Error -> {
                            binding.btnCreate.isEnabled = false
                            binding.btnCreate.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.button_blur
                            )
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun createUser() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val phoneNumber = binding.inputPhoneNumber.text.toString().trim()
        
        viewModel.createUser(username, password, phoneNumber)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnCreate.isEnabled = false
                        binding.btnCreate.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button_blur
                        )
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        binding.btnCreate.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button
                        )
                        Toast.makeText(requireContext(), "Create user successful!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        binding.btnCreate.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button
                        )
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateButtonState(){
        val valid = !binding.inputUsername.text?.trim().toString().isNullOrEmpty() &&
                !binding.inputPassword.text?.trim().toString().isNullOrEmpty()

        if(valid){
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
            binding.btnCreate.isEnabled = true
        }
        else{
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
            binding.btnCreate.isEnabled = false
        }
    }

    private fun updateUser() {
        val phoneNumber = binding.inputPhoneNumber.text.toString().trim()

        viewModel.updateUser(phoneNumber)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnCreate.isEnabled = false
                        binding.btnCreate.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button_blur
                        )
                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        binding.btnCreate.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button
                        )
                        Toast.makeText(requireContext(), "Update user successful!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        binding.btnCreate.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button
                        )
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