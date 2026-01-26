package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.models.BookingDetails
import com.example.glowpoint.domain.model.result.BookingResult
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun bookServices(booking: BookingDetails): BookingResult

    fun observeBookings(userId: String): Flow<BookingResult>

    fun observeBooking(bookingId: String): Flow<BookingResult>

    suspend fun cancelBooking(previousStatus: String, bookingId: String): BookingResult
}