package com.example.firealarm.presentation.ui.admin.firmware.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firealarm.databinding.FragmentFirmwareDetailBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class FirmwareDetailFragment : Fragment() {
    private var _binding: FragmentFirmwareDetailBinding? = null
    private val binding get() = _binding!!
    private val args: FirmwareDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirmwareDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(com.example.firealarm.R.id.bottom_nav)?.visibility = View.GONE

        setupViews()

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupViews() {
        binding.textVersion.text = args.version
        binding.textVersionNumber.text = args.versionNumber.toString()

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(args.releasedAt)
            binding.textReleasedAt.text = date?.let { outputFormat.format(it) } ?: args.releasedAt
        } catch (e: Exception) {
            binding.textReleasedAt.text = args.releasedAt
        }
        
        binding.textUrl.text = args.downloadUrl
        binding.textDescription.text = args.description
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}