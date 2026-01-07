package com.example.glowpoint.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.R
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.databinding.ItemBookingHistoryBinding
import com.example.glowpoint.util.BookingStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingHistoryAdapter(private val onItemClicked: (FetchedBooking) -> Unit) :
    ListAdapter<FetchedBooking, BookingHistoryAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding =
            ItemBookingHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: ItemBookingHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set the listener once when the ViewHolder is created. This is more efficient.
            binding.root.setOnClickListener {
                // Use bindingAdapterPosition to safely get the position of the clicked item.
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(booking: FetchedBooking) {
            binding.shopNameTextView.text = booking.shopName
            var servicesName = ""
            booking.selectedServices.forEach {
                servicesName += it.name
            }
            binding.serviceTextView.text = servicesName

            val bookingStatus = booking.bookingStatus
            if (bookingStatus == BookingStatus.CANCELLED) {
                binding.statusChip.setChipBackgroundColorResource(com.example.glowpoint.R.color.red)
            } else {
                binding.statusChip.setChipBackgroundColorResource(com.example.glowpoint.R.color.green)
            }
            binding.statusChip.text = bookingStatus
            val bookingSlots = booking.selectedTimeSlot
            val slotText = bookingSlots
                .take(2)
                .joinToString(", ")
                .let { if (bookingSlots.size > 2) "$it, ..." else it }
            binding.dateTextView.text = formatDate(booking.createdAt) + " at " + slotText
        }

        private fun formatDate(dateMillis: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return formatter.format(Date(dateMillis))
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<FetchedBooking>() {
        override fun areItemsTheSame(oldItem: FetchedBooking, newItem: FetchedBooking): Boolean {
            return oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(oldItem: FetchedBooking, newItem: FetchedBooking): Boolean {
            return oldItem == newItem
        }
    }
}