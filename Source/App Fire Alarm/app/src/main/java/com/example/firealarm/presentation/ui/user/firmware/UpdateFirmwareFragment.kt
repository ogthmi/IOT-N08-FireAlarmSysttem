package com.example.firealarm.presentation.ui.user.firmware

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentUpdateFirmwareBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFirmwareFragment : Fragment() {
    private var _binding: FragmentUpdateFirmwareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateFirmwareBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        val categories = listOf("v.1.0.0", "v.1.1.0", "v.1.1.2", "v.1.1.3")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            categories
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spinner.adapter = adapter
        binding.spinner.setSelection(0)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = categories[position]
                Toast.makeText(requireContext(), "Chọn: $selected", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            // TODO: Nếu đang cập nhập firmware thì hủy cập nhật
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            // TODO: Cập nhật firmware
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}