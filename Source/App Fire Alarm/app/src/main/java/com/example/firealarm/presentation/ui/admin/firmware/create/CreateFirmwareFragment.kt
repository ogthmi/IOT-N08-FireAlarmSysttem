package com.example.firealarm.presentation.ui.admin.firmware.create

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firealarm.R
import com.example.firealarm.databinding.FragmentCreateFirmwareBinding
import com.example.firealarm.presentation.utils.NetworkState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class CreateFirmwareFragment : Fragment() {
    private var _binding: FragmentCreateFirmwareBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateFirmwareViewModel by viewModels()
    private val args: CreateFirmwareFragmentArgs by navArgs()
    private val isEditMode: Boolean get() = args.edit

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val fileName = getFileName(it)
                binding.textFileName.text = fileName ?: "File selected"
                selectedFileUri = it
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private var selectedFileUri: Uri? = null

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFirmwareBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE

        if (isEditMode) {
            binding.title.text = getString(R.string.update_firmware)
            binding.inputVersion.setText(args.version)
            binding.inputVersionNumber.setText(args.versionNumber.toString())
            binding.inputDescription.setText(args.description)
            binding.cardFile.visibility = View.GONE
            binding.btnCreate.text = getString(R.string.update)
            binding.btnCreate.isEnabled = true
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)

        }

        val inputs = listOf(
            binding.inputVersion,
            binding.inputVersionNumber
        )

        inputs.forEach {editText ->
            editText.addTextChangedListener {
                updateButtonState()
            }
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSelectFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnCreate.setOnClickListener {
            if (isEditMode) {
                updateFirmware()
            } else {
                createFirmware()
            }
        }
    }

    private fun createFirmware() {
        val version = binding.inputVersion.text?.toString()?.trim()
        val versionNumberStr = binding.inputVersionNumber.text?.toString()?.trim()
        val description = binding.inputDescription.text?.toString()?.trim()

        when {
            selectedFileUri == null -> {
                Toast.makeText(requireContext(), "Please select a firmware file", Toast.LENGTH_SHORT).show()
            }
            version.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Please enter version", Toast.LENGTH_SHORT).show()
            }
            versionNumberStr.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Please enter version number", Toast.LENGTH_SHORT).show()
            }
            else -> {
                try {
                    val versionNumber = versionNumberStr.toInt()
                    val file = uriToFile(selectedFileUri!!)
                    if (file != null && file.exists()) {
                        viewModel.uploadFirmware(
                            file = file,
                            version = version,
                            versionNumber = versionNumber,
                            description = description ?: ""
                        )
                        setupObservers()
                    } else {
                        Toast.makeText(requireContext(), "Error accessing file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Version number must be a valid integer", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFirmware() {
        val version = binding.inputVersion.text?.toString()?.trim()
        val versionNumberStr = binding.inputVersionNumber.text?.toString()?.trim()
        val description = binding.inputDescription.text?.toString()?.trim()

        when {
            version.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Please enter version", Toast.LENGTH_SHORT).show()
            }
            versionNumberStr.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Please enter version number", Toast.LENGTH_SHORT).show()
            }
            else -> {
                try {
                    val versionNumber = versionNumberStr.toInt()
                    viewModel.updateFirmware(
                        id = args.firmwareId,
                        version = version,
                        versionNumber = versionNumber,
                        description = description ?: ""
                    )
                    setupObservers()
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Version number must be a valid integer", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun uriToFile(uri: Uri): File? {
        return try {
            // Try to get file path directly
            val path = uri.path
            if (path != null) {
                val file = File(path)
                if (file.exists()) return file
            }
            
            // If direct path doesn't work, copy from content URI to temp file
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = getFileName(uri) ?: "firmware.bin"
            val tempFile = File(requireContext().cacheDir, fileName)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.exists()) tempFile else null
        } catch (e: Exception) {
            null
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadState.collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnCreate.isEnabled = false
                        binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)

                    }
                    is NetworkState.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)

                        val message = if (isEditMode) "Firmware updated successfully!" else "Firmware uploaded successfully!"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is NetworkState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCreate.isEnabled = true
                        binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)

                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateButtonState(){
        val valid = !binding.inputVersion.text.toString().isNullOrEmpty() &&
                !binding.inputVersionNumber.text.toString().isNullOrEmpty() &&
                !selectedFileUri.toString().isNullOrEmpty()

        if(valid){
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button)
            binding.btnCreate.isEnabled = true
        }
        else{
            binding.btnCreate.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_blur)
            binding.btnCreate.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}