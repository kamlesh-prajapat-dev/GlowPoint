package com.example.glowpoint.data.models

import com.example.glowpoint.util.BookingStatus

data class FetchedBooking(
    val bookingId: String = "",
    val userId: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val shopAddress: String = "",
    val distance: Double = 0.0,
    val selectedServices: List<ServiceItem> = emptyList(),
    val selectedTimeSlot: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val bookingStatus: String = "",
    val previousStatus: String = BookingStatus.PENDING,
    val paymentDetails: PaymentDetails = PaymentDetails()
)
