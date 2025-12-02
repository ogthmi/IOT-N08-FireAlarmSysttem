package com.example.firealarm.presentation.ui.user.chooseDevice

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
import androidx.navigation.fragment.navArgs
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentChooseDeviceBinding
import com.example.firealarm.presentation.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseDeviceFragment : Fragment() {
    private var _binding: FragmentChooseDeviceBinding? = null
    private val binding get() = _binding!!
    private val args: ChooseDeviceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseDeviceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        val mes = args.mes
        if(mes.equals("fromSetting")){
            binding.title.setText("Thay đổi thiết bị")
            binding.btnContinue.setText("Thay đổi")
            binding.closeBtn.visibility = View.VISIBLE
        }

        val categories = listOf("Device 1", "Device 2", "Device 3", "Device 4")

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

        binding.btnContinue.setOnClickListener {
            if(mes.equals("fromSetting")) findNavController().navigate(
                ChooseDeviceFragmentDirections.actionChooseDeviceFragment2ToHomeFragment())
            else (requireActivity() as MainActivity).switchToMainUserGraph()
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}