package com.example.firealarm.presentation.ui.login

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentLoginBinding
import com.example.firealarm.presentation.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
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
            checkLogin()
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

    private fun checkLogin(){
        val username = binding.inputUsername.text.toString()
        val password = binding.inputPassword.text.toString()

        // TODO: Check login by role
        // Fake data
        if(username.equals("admin") && password.equals("1234")){
            (requireActivity() as MainActivity).switchToMainAdminGraph()
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
            bottomNav?.menu?.clear()
            bottomNav?.inflateMenu(R.menu.admin_bottom_menu)
            bottomNav?.visibility = View.VISIBLE
        }
        else{
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChooseDeviceFragment(""))
        }

    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}