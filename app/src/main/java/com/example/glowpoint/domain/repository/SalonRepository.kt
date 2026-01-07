package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.model.FetchSalonsResult
import com.firebase.geofire.GeoQueryBounds
import kotlinx.coroutines.flow.Flow

interface SalonRepository {
    suspend fun getNearBySalon(
        centerLat: Double,
        centerLng: Double,
        bounds: List<GeoQueryBounds>
    ): FetchSalonsResult

    fun observeTimeSlot(salonId: String, date: Long, openTime: String, closeTime: String): Flow<FetchSalonsResult>

    suspend fun updateTimeSlot(salonId: String, date: Long, timeSlot: List<TimeSlot>): FetchSalonsResult
}
