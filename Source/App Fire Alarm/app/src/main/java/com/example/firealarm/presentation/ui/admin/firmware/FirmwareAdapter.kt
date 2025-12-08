package com.example.firealarm.presentation.ui.admin.firmware

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firealarm.R
import com.example.firealarm.databinding.ItemFirmwareBinding
import com.example.firealarm.domain.model.Device
import com.example.firealarm.domain.model.Firmware
import java.text.SimpleDateFormat
import java.util.Locale

class FirmwareAdapter(
    private val onItemClick: (Firmware) -> Unit,
    private val onEditClick: (Firmware) -> Unit,
    private val onDeleteClick: (Firmware) -> Unit
) : ListAdapter<Firmware, FirmwareAdapter.FirmwareViewHolder>(FirmwareDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirmwareViewHolder {
        val binding = ItemFirmwareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FirmwareViewHolder(binding, onItemClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: FirmwareViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FirmwareViewHolder(
        private val binding: ItemFirmwareBinding,
        private val onItemClick: (Firmware) -> Unit,
        private val onEditClick: (Firmware) -> Unit,
        private val onDeleteClick: (Firmware) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(firmware: Firmware) {
            binding.textVersion.text = "Version ${firmware.version}"

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(firmware.releasedAt)
                binding.textReleasedAt.text = "Release at: ${date?.let { outputFormat.format(it) } ?: firmware.releasedAt}"

                binding.btnMenu.setOnClickListener { view ->
                    val popupMenu = PopupMenu(view.context, view)
                    popupMenu.menuInflater.inflate(R.menu.device_sub_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_edit -> {
                                onEditClick(firmware)
                                true
                            }
                            R.id.menu_delete -> {
                                onDeleteClick(firmware)
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }
            } catch (e: Exception) {
                binding.textReleasedAt.text = firmware.releasedAt
            }
            
            binding.root.setOnClickListener {
                onItemClick(firmware)
            }
        }
    }

    class FirmwareDiffCallback : DiffUtil.ItemCallback<Firmware>() {
        override fun areItemsTheSame(oldItem: Firmware, newItem: Firmware): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Firmware, newItem: Firmware): Boolean {
            return oldItem == newItem
        }
    }
}

