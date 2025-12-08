package com.example.firealarm.presentation.ui.login

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentLoginBinding
import com.example.firealarm.domain.model.User
import com.example.firealarm.presentation.MainActivity
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.utils.Constant
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private val TAG = "LoginFragment"
    private var hasNavigated = false
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        val inputs = listOf(
            binding.inputUsername,
            binding.inputPassword
        )

        inputs.forEach {editText ->
            editText.addTextChangedListener {
                updateButtonState()
            }
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.inputUsername.text.toString()
            val password = binding.inputPassword.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                signIn(username, password)
            }
        }
    }

    private fun signIn(username: String, password: String) {
        hasNavigated = false
        viewModel.login(username = username, password = password)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                if (hasNavigated) {
                    return@collect
                }
                
                when (state) {
                    is NetworkState.Init, is NetworkState.Loading -> {
                        binding.btnLogin.isEnabled = false
                        binding.btnLogin.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
                        binding.loading.visibility = View.VISIBLE
                    }
                    is NetworkState.Success<*> -> {
                        if (!isAdded || !isResumed) {
                            return@collect
                        }
                        
                        binding.btnLogin.isEnabled = true
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                        val user = state.data as User
                        if (user != null) {
                            AppPreferences.saveToken(user.accessToken)
                            AppPreferences.saveUsername(username)
                            val role = user.role
                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.loginFragment, true)
                                .build()

                            // Check role
                            if(role.equals(Constant.admin)){
                                hasNavigated = true
                                (requireActivity() as MainActivity).switchToMainAdminGraph()
                                val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
                                bottomNav?.menu?.clear()
                                bottomNav?.inflateMenu(R.menu.admin_bottom_menu)
                                bottomNav?.visibility = View.VISIBLE
                                return@collect
                            }
                            else{
                                hasNavigated = true
                                val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
                                bottomNav?.menu?.clear()
                                bottomNav?.inflateMenu(R.menu.user_bottom_menu)
                                findNavController().navigate(
                                    LoginFragmentDirections.actionLoginFragmentToChooseDeviceFragment(""),
                                    navOptions
                                )
                            }
                        }
                    }
                    is NetworkState.Error -> {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Username or password is incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateButtonState(){
        val valid = !binding.inputUsername.text.toString().isNullOrEmpty() &&
                !binding.inputPassword.text.toString().isNullOrEmpty()

        if(valid){
            binding.btnLogin.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
            binding.btnLogin.isEnabled = true
        }
        else{
            binding.btnLogin.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
            binding.btnLogin.isEnabled = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}