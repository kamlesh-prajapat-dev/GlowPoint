package com.example.glowpoint.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.databinding.ItemSelectedServiceBinding
import com.example.glowpoint.data.models.ServiceItem

class SelectedServicesAdapter : ListAdapter<ServiceItem, SelectedServicesAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemSelectedServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServiceViewHolder(private val binding: ItemSelectedServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: ServiceItem) {
            binding.serviceName.text = service.name
            binding.servicePrice.text = "₹ ${service.price}"
        }
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<ServiceItem>() {
        override fun areItemsTheSame(oldItem: ServiceItem, newItem: ServiceItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ServiceItem, newItem: ServiceItem): Boolean {
            return oldItem == newItem
        }
    }
}