package com.example.glowpoint.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.R
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.databinding.TimeSlotChipBinding

class RecyclerViewTimeSlotAdapter(
    private val onTimeSlotClick: (TimeSlot) -> Unit
) : ListAdapter<TimeSlot, RecyclerViewTimeSlotAdapter.ViewHolder>(TimeSlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TimeSlotChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: TimeSlotChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val timeSlot = getItem(position)
                    // Only allow clicks on available slots
                    if (timeSlot.isAvailable) {
                        onTimeSlotClick(timeSlot)
                    }
                }
            }
        }

        fun bind(item: TimeSlot) {
            binding.root.text = item.time
            val context = binding.root.context

            if (!item.isAvailable) {
                // --- UNAVAILABLE STATE ---
                binding.root.isEnabled = false
                binding.root.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                binding.root.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.root.isEnabled = true
                if (item.isSelected) {
                    // --- SELECTED STATE ---
                    binding.root.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_500)) // Example selected color
                    binding.root.setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    // --- AVAILABLE (DEFAULT) STATE ---
                    binding.root.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
                    binding.root.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
            }
        }
    }
}

class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
    override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
        return oldItem.time == newItem.time
    }

    override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
        return oldItem == newItem
    }
}
