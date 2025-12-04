package com.example.firealarm.presentation.ui.admin.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firealarm.R
import com.example.firealarm.databinding.ItemUserBinding
import com.example.firealarm.domain.model.UserInfo

class UserAdapter(
    private val onItemClick: (UserInfo) -> Unit,
    private val onDeleteClick: (UserInfo) -> Unit
) : ListAdapter<UserInfo, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onItemClick: (UserInfo) -> Unit,
        private val onDeleteClick: (UserInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserInfo) {
            binding.textUsername.text = user.username
            binding.textDeviceCount.text = "${user.deviceCount} thiết bị"
            
            binding.root.setOnClickListener {
                onItemClick(user)
            }
            
            binding.btnMenu.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.user_sub_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_delete -> {
                            onDeleteClick(user)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }
}

