package com.example.glowpoint.domain.model

import com.example.glowpoint.data.models.FetchedBooking

interface BookingResult {
    data class Success(val bookingId: String) : BookingResult
    data class Failure(val exception: Exception) : BookingResult
    data class GetSuccess(val bookings: List<FetchedBooking>): BookingResult
    data class GetFetchedBookingSuccess(val booking: FetchedBooking): BookingResult
    data class CancelSuccess(val isSuccess: Boolean): BookingResult
}