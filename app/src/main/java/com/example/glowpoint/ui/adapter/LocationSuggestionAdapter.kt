package com.example.glowpoint.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.data.models.LocationSuggestion
import com.example.glowpoint.databinding.ItemLocationSuggestionBinding

class LocationSuggestionAdapter(
    private val onItemClick: (LocationSuggestion) -> Unit
) : ListAdapter<LocationSuggestion, LocationSuggestionAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class LocationViewHolder(val binding: ItemLocationSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocationSuggestion) {
            binding.tvPlaceName.text = item.name
            binding.tvPlaceDetails.text = item.details

            binding.itemLocationSuggestionCard.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LocationSuggestion>() {

            override fun areItemsTheSame(
                oldItem: LocationSuggestion,
                newItem: LocationSuggestion
            ): Boolean {
                // Unique enough for location suggestions
                return oldItem.latitude == newItem.latitude &&
                        oldItem.longitude == newItem.longitude
            }

            override fun areContentsTheSame(
                oldItem: LocationSuggestion,
                newItem: LocationSuggestion
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
