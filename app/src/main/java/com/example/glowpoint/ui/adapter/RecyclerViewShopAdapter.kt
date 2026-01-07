package com.example.glowpoint.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.R
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.databinding.ShopItemBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class RecyclerViewShopAdapter(
    private val onShopClick: (ShopDetails) -> Unit
) : ListAdapter<ShopDetails, RecyclerViewShopAdapter.ViewHolder>(ShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ShopItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ShopItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set the listener once when the ViewHolder is created. This is more efficient.
            binding.root.setOnClickListener {
                // Use bindingAdapterPosition to safely get the position of the clicked item.
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onShopClick(getItem(position))
                }
            }
        }

        fun bind(salon: ShopDetails) {
            binding.shopName.text = salon.name
            binding.shopRatting.text = salon.rating.toString()
            binding.shopDistance.text = (salon.distance.div(1000)).let { "%.2f km away".format(it) }

            val isOpen = isShopOpenNow(salon.openTime, salon.closeTime)
            val context = binding.root.context
            if (isOpen) {
                binding.shopStatus.text = "Open Now"
                binding.shopStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
            } else {
                binding.shopStatus.text = "Closed Now"
                binding.shopStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            // The click listener is no longer set here.
        }
    }

    private fun isShopOpenNow(openTimeStr: String?, closeTimeStr: String?): Boolean {
        if (openTimeStr.isNullOrBlank() || closeTimeStr.isNullOrBlank()) {
            return false
        }

        return try {
            // Important: This formatter expects the time string to be in a specific format,
            // for example, "09:00AM" or "10:30PM".
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val openTime = LocalTime.parse(openTimeStr, formatter)
            val closeTime = LocalTime.parse(closeTimeStr, formatter)
            val currentTime = LocalTime.now()

            !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime)
        } catch (e: DateTimeParseException) {
            // Log the error for debugging if the format from Firestore is incorrect.
            android.util.Log.e("ShopAdapter", "Invalid time format for shop: $openTimeStr, $closeTimeStr", e)
            false
        }
    }
}

class ShopDiffCallback : DiffUtil.ItemCallback<ShopDetails>() {
    override fun areItemsTheSame(oldItem: ShopDetails, newItem: ShopDetails): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ShopDetails, newItem: ShopDetails): Boolean {
        return oldItem == newItem
    }
}
