package com.example.glowpoint.data.models

import com.example.glowpoint.util.BookingStatus

data class BookingDetails(
    val userId: String,
    val shopId: String,
    val shopName: String,
    val shopAddress: String,
    val distance: Double,
    val selectedServices: List<ServiceItem>,
    val selectedTimeSlot: List<String>,
    val createdAt: Long,
    val bookingStatus: String,
    val previousStatus: String = BookingStatus.PENDING,
    val paymentDetails: PaymentDetails
)