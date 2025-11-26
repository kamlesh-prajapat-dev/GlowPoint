package com.example.glowpoint.domain.repository

import com.example.glowpoint.domain.model.FetchSalonsResult

interface SalonRepository {
    suspend fun getNearBySalon(centerLat: Double, centerLng: Double): FetchSalonsResult
}
