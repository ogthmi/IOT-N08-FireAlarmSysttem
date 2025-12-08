package com.example.firealarm.presentation.ui.admin.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firealarm.R
import com.example.firealarm.databinding.ItemDeviceBinding
import com.example.firealarm.domain.model.Device

class DeviceAdapter(
    private val onItemClick: (Device) -> Unit,
    private val onEditClick: (Device) -> Unit,
    private val onDeleteClick: (Device) -> Unit
) : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onItemClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onItemClick: (Device) -> Unit,
        private val onEditClick: (Device) -> Unit,
        private val onDeleteClick: (Device) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.textDeviceName.text = device.deviceName
            binding.textDeviceId.text = "ID: ${device.deviceId}"
            
            binding.root.setOnClickListener {
                onItemClick(device)
            }

            binding.btnMenu.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.device_sub_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onEditClick(device)
                            true
                        }
                        R.id.menu_delete -> {
                            onDeleteClick(device)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.deviceId == newItem.deviceId
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem == newItem
        }
    }
}

