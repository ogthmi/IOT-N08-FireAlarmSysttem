
package com.example.firealarm.presentation.ui.admin.user

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentUserListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListFragment : Fragment() {
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    
    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchEditText = binding.search.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        searchEditText.setHintTextColor(R.color.background)
        searchEditText.setTextColor(Color.WHITE)

        binding.addDevice.setOnClickListener {
            findNavController().navigate(UserListFragmentDirections.actionUserListFragmentToCreateDeviceFragment())
        }


    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}