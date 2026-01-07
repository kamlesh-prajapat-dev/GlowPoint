package com.example.glowpoint.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.R
import com.example.glowpoint.databinding.ServiceItemBinding
import com.example.glowpoint.data.models.ServiceItem

class RecyclerViewItemAdapter(
    private val onAddClick: (ServiceItem, genderCategory: Boolean) -> Unit
) : ListAdapter<ServiceItem, RecyclerViewItemAdapter.ViewHolder>(ServiceItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ServiceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ServiceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ServiceItem) {
            binding.serviceName.text = item.name
            binding.serviceDescription.text = item.description
            binding.servicePrice.text = "₹ " + item.price.toString()
            binding.serviceDuration.text = item.duration

            val context = binding.root.context
            if (!item.isSelected) {
                binding.addButton.text = context.getString(R.string.addBtn_when_not_select)
                binding.addButton.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                binding.addButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.addButton.text = context.getString(R.string.addBtn_when_elect)
                binding.addButton.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                binding.addButton.setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            binding.addButton.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val item = getItem(bindingAdapterPosition)
                    onAddClick(item, item.genderCategory)
                }
            }
        }
    }
}

class ServiceItemDiffCallback : DiffUtil.ItemCallback<ServiceItem>() {
    override fun areItemsTheSame(oldItem: ServiceItem, newItem: ServiceItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ServiceItem, newItem: ServiceItem): Boolean {
        return oldItem == newItem
    }
}
